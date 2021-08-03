package com.ilareguy.spear.oauth;

import com.ilareguy.spear.App;
import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.Task;
import com.ilareguy.spear.TaskResult;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.Request;
import okhttp3.Response;

public class Communicator{

    public interface OAuthRequestComplete{
        void onComplete(final @NonNull TaskResult<Response> result);
    }

    private static final class _Task extends Task<_Task.Args, Void, Response>{

        _Task(final @Nullable PageAbstract callingPage){
            super(callingPage);
        }

        public static final class Args{
            final RequestAbstract request;
            final OAuthRequestComplete complete;
            final Communicator communicator;

            public Args(final Communicator communicator,
                        final RequestAbstract request,
                        final OAuthRequestComplete complete){
                this.request = request;
                this.complete = complete;
                this.communicator = communicator;
            }
        }

        private OAuthRequestComplete complete;

        @Override
        public final @NonNull
        TaskResult<Response> doInBackground(Args... a){
            final Args args = a[0];
            this.complete = args.complete;
            return args.communicator.sendRequest(args.request);
        }

        @Override
        public final void onPostExecute(final @NonNull TaskResult<Response> v){
            complete.onComplete(v);
        }
    }

    public Communicator(){}

    public void sendRequest(final RequestAbstract request, final OAuthRequestComplete complete){
        new _Task(request.getCallingPage()).execute(new _Task.Args(this, request, complete));
    }

    public @NonNull TaskResult<Response> sendRequest(RequestAbstract request){
        // Sign the request
        request.sign();

        // Create the OkHttp request object
        Request ok_request = request.buildOkHttpRequest();

        // Send
        try{
            return new TaskResult<>(App.getOkHttpClient().newCall(ok_request).execute());
        }catch(IOException e){
            e.printStackTrace();
            return new TaskResult<>(SpearError.build(e));
        }
    }
}
