package com.ilareguy.spear.util;

import com.ilareguy.spear.App;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.RequestBody;
import okio.Buffer;

public abstract class NetworkHelper{

    public static void cancelAllRequestsWithTag(final int tag){
        final Dispatcher okhttp_dispatcher = App.getOkHttpClient().dispatcher();
        _doCancelRequestsWithTag(tag, okhttp_dispatcher.queuedCalls());
        _doCancelRequestsWithTag(tag, okhttp_dispatcher.runningCalls());
    }

    private static void _doCancelRequestsWithTag(final int tag, final List<Call> calls){
        for(Call call : calls){
            if(call.request().tag() != null
                    && call.request().tag().equals(tag)){
                // Found a match; cancel it
                call.cancel();
            }
        }
    }

    public static String getOkHttp3RequestBodyString(final RequestBody requestBody){
        try {
            final Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "[did not work]";
        }
    }

}
