package com.ilareguy.spear;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class App extends Application {
    private static final String LOG_TAG = "SPEAR";

    private static App instance;
    private static OkHttpClient okHttpClient;

    private boolean init = false;
    private ActivityAbstract activityInstance; // Each Spear application runs on a single activity
    private ConnectivityManager connectivityManager;

    public App(){
        super();
        instance = this;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        // Perform async initialization
        final Thread init_thread = new Thread(){
            @Override
            public void run(){
                asyncInitialize();
            }
        };
        init_thread.start();

        // Initialize OkHttp
        initOkHttp();
        // Initialize ConnectivityManager
        initConnectivityManager();
        // Initialize Fresco
        initFresco();

        // Wait for async init thread to complete
        try {
            init_thread.join();
        } catch(InterruptedException e){
            e.printStackTrace();
        }

        init = true;
    }

    public void setActivityInstance(final ActivityAbstract activityInstance){ this.activityInstance = activityInstance; }

    /**
     * Implement this method to initialize anything that needs to be initialize for your app to
     * function properly. This will be called from a separate thread to allow interaction with a
     * Room database, but it will act in a blocking manner. Only perform quick tasks as any lasting
     * operation will block the UI thread.
     *
     * Be sure to call super.asyncInitialize(), otherwise expect something to go wrong.
     *
     * TODO: 2018-08-18 Implement another way to take care of this. This is poor design decision.
     */
    @WorkerThread
    protected void asyncInitialize(){
        //
    }

    private void initOkHttp(){
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(Config.OAUTH_REQUEST_TIMEOUT, TimeUnit.SECONDS);
        //if(BuildConfig.DEBUG)
        //    builder.addInterceptor(new LoggingInterceptor());
        okHttpClient = builder.build();
    }

    private void initConnectivityManager(){
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void initFresco(){
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                .newBuilder(this, okHttpClient)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);
    }

    public boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    public ActivityAbstract getActivityInstance() { return activityInstance; }

    public static @Nullable Response sendNetworkRequest(final Request r){
        try{
            return okHttpClient.newCall(r).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static void cancelAllNetworkRequests(){
        for(Call call : okHttpClient.dispatcher().runningCalls())
            call.cancel();
        for(Call call : okHttpClient.dispatcher().queuedCalls())
            call.cancel();
    }

    public static void cancelNetworkRequestsWithTag(final String tag){
        for(Call call : okHttpClient.dispatcher().runningCalls()) {
            if (call.request().tag().equals(tag))
                call.cancel();
        }
        for(Call call : okHttpClient.dispatcher().queuedCalls()) {
            if (call.request().tag().equals(tag))
                call.cancel();
        }
    }

    public static OkHttpClient getOkHttpClient(){ return okHttpClient; }
    public static App getInstance(){ return instance; }
    public static void _d(String str) {
        Log.d(LOG_TAG, str);
    }
    public static void _e(String str) {
        Log.e(LOG_TAG, str);
    }

    public static final class LoggingInterceptor implements Interceptor{
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Log.d("OkHttp", String.format("--> Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()));

            if(request.body() != null){
                Buffer requestBuffer = new Buffer();
                request.body().writeTo(requestBuffer);
                Log.d("OkHttp", requestBuffer.readUtf8());
            }

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Log.d("OkHttp", String.format("<-- Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            MediaType contentType = response.body().contentType();
            String content = response.body().string();
            Log.d("OkHttp", content);

            ResponseBody wrappedBody = ResponseBody.create(contentType, content);
            return response.newBuilder().body(wrappedBody).build();
        }
    }

}
