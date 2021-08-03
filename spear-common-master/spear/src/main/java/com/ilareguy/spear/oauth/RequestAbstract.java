package com.ilareguy.spear.oauth;

import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.util.StringHelper;

import java.util.SortedMap;
import java.util.TreeMap;

import androidx.annotation.Nullable;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

public abstract class RequestAbstract{

    // All these are supported by OkHttp
    public enum Method{
        POST,
        GET,
        HEAD,
        DELETE,
        PUT,
        PATCH,
    }

    /**
     * @return A new random nonce.
     */
    public static String getNonce(){
        return new StringHelper.RandomString(21).nextString();
    }

    public static String getMethodString(RequestAbstract.Method method){
        switch(method){
            case POST:
                return "POST";
            case GET:
                return "GET";
            case DELETE:
                return "DELETE";
            case PUT:
                return "PUT";
            case PATCH:
                return "PATCH";
        }

        return "";
    }

    private int mTag = 0;
    private final ConsumerKey mConsumerKey;
    private String mURL;
    private BasicRequest.Method mMethod;
    private SortedMap<String, Parameter> mParameters = new TreeMap<>();
    private boolean requiresSigning = true;
    private @Nullable PageAbstract callingPage = null;

    public RequestAbstract(ConsumerKey consumer_key, BasicRequest.Method method, String url){
        mURL = url;
        mMethod = method;
        mConsumerKey = consumer_key;
    }

    public okhttp3.Request buildOkHttpRequest(){
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        builder.tag(getTag());

        if(mMethod == RequestAbstract.Method.GET || mMethod == RequestAbstract.Method.PUT){
            HttpUrl.Builder url_builder = HttpUrl.parse(mURL).newBuilder();
            for(SortedMap.Entry<String, Parameter> param : getParametersMap().entrySet())
                url_builder.addQueryParameter(param.getKey(), param.getValue().value);
            builder.url(url_builder.build());
        }else{
            builder.url(mURL);
            FormBody.Builder request_body_builder = new FormBody.Builder();
            for(SortedMap.Entry<String, Parameter> param : getParametersMap().entrySet())
                request_body_builder.add(param.getKey(), param.getValue().value);
            switch(mMethod){
                case POST:
                    builder.post(request_body_builder.build());
                    break;
                case PUT:
                    builder.put(request_body_builder.build());
                    break;
                case DELETE:
                    builder.delete(request_body_builder.build());
                    break;
                case PATCH:
                    builder.patch(request_body_builder.build());
            }
        }

        return builder.build();
    }

    /**
     * Adds a new parameter that will be sent along with the OAuthRequest.
     * This methods performs no check of whether or not a parameter with
     * the given name already exists.
     *
     * @param name Name of the parameter.
     * @param value Value of the parameter.
     * @param usedInSigning True if this parameter is used to generate an OAuth signature.
     */
    public void addParameter(String name, String value, boolean usedInSigning){
        this.mParameters.put(name, new Parameter(value, usedInSigning));
    }

    public void addParameter(String name, String value){
        addParameter(name, value, true);
    }

    public final String getUrl(){ return mURL; }
    public final BasicRequest.Method getMethod(){ return this.mMethod; }
    public final SortedMap<String, Parameter> getParametersMap(){ return mParameters; }
    public final ConsumerKey getConsumerKey(){ return mConsumerKey; }
    public boolean requiresSigning(){ return requiresSigning; }
    public void setRequiresSigning(boolean r){ requiresSigning = r; }
    public void setTag(final int t){ mTag = t; }
    public void setTag(final String str){ setTag(str.hashCode()); }
    public final int getTag(){ return mTag; }
    public final void setCallingPage(final @Nullable PageAbstract page){ this.callingPage = page; }
    public final @Nullable PageAbstract getCallingPage(){ return callingPage; }

    public abstract void sign();

    public static final class Parameter{
        public final String value;
        public final boolean useInSigning;

        public Parameter(String value){
            this(value, true);
        }

        public Parameter(String value, boolean useInSigning){
            this.value = value;
            this.useInSigning = useInSigning;
        }
    }
}
