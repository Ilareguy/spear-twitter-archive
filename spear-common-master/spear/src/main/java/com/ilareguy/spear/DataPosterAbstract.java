package com.ilareguy.spear;

import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.RequestAbstract;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Response;

import static com.ilareguy.spear.SpearErrorCode.OK_HTTP_ERROR;

public abstract class DataPosterAbstract{
    public interface OnPostExecuteListener{
        void onPostExecute(final @NonNull TaskResult<String> result);
    }

    private OnPostExecuteListener onPostExecuteListener;

    /**
     * Your implementation should override this.
     * @return Returns the oauth request to be sent to a remote server.
     */
    protected abstract @NonNull
    RequestAbstract buildRequest();

    private static TaskResult<String> okHttpResponseToResult(final Response response) throws IOException{
        if(response != null && response.body() != null && response.isSuccessful())
            return new TaskResult<>(response.body().string());
        else{
            if(response != null && response.body() != null)
                return new TaskResult<>(SpearError.build(OK_HTTP_ERROR, response.body().string()));
            else
                return new TaskResult<>(SpearError.build(OK_HTTP_ERROR));
        }
    }

    public TaskResult<String> execute(){
        if(!App.getInstance().isNetworkAvailable()){
            // No internet connection
            return new TaskResult<>(SpearError.build(SpearErrorCode.INTERNET_UNAVAILABLE));
        }

        RequestAbstract oauth_request = buildRequest();
        OAuth.getInstance().getGlobalCommunicator().sendRequest(oauth_request);

        try {
            return okHttpResponseToResult(App.sendNetworkRequest(oauth_request.buildOkHttpRequest()));
        } catch (IOException e) {
            return new TaskResult<>(SpearError.build(e));
        }
    }

    public void asyncExecute(OnPostExecuteListener onPostExecuteListener){
        this.onPostExecuteListener = onPostExecuteListener;
    }

    private OnPostExecuteListener getOnPostExecuteListener(){ return onPostExecuteListener; }

}
