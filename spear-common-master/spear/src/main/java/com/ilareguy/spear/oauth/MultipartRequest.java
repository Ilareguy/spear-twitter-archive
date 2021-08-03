package com.ilareguy.spear.oauth;

import com.ilareguy.spear.util.StringHelper;
import com.ilareguy.spear.util.Timestamp;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.SortedMap;
import java.util.TreeMap;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MultipartRequest extends BasicRequest{

    private SortedMap<String, StreamParameterAbstract> mStreamedParameters = new TreeMap<>();
    private String authorizationHeader = "";

    public MultipartRequest(final ConsumerKey consumer_key, final AccessToken access_token,
                            String url){
        super(consumer_key, access_token, Method.POST, url);
    }

    @Override
    public okhttp3.Request buildOkHttpRequest(){
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        final RequestBody requestBody = buildRequestBody();

        builder.tag(getTag());
        builder.url(getUrl());
        builder.addHeader("Authorization", authorizationHeader);
        builder.post(requestBody);

        return builder.build();
    }

    @Override
    public void sign(){
        if(!requiresSigning()) return;
        final StringBuilder s = new StringBuilder();
        final SortedMap<String, RequestAbstract.Parameter> oauth_params = new TreeMap<>();

        oauth_params.put("oauth_version",
                new RequestAbstract.Parameter("1.0"));
        oauth_params.put("oauth_nonce",
                new RequestAbstract.Parameter(String.valueOf(RequestAbstract.getNonce())));
        oauth_params.put("oauth_timestamp",
                new RequestAbstract.Parameter(String.valueOf(Timestamp.now())));
        oauth_params.put("oauth_consumer_key",
                new RequestAbstract.Parameter(getConsumerKey().getKey()));
        oauth_params.put("oauth_token",
                new RequestAbstract.Parameter(getAccessToken().getToken()));
        oauth_params.put("oauth_signature_method",
                new RequestAbstract.Parameter("HMAC-SHA1"));

        // Generate a signature
        final String oauth_signature
                = SignatureGenerator.getSignature(
                        getMethod(), getUrl(), getConsumerKey(), getAccessToken(), oauth_params);
        oauth_params.put("oauth_signature", new RequestAbstract.Parameter(oauth_signature, false));

        // Generate the authorization header
        s.append("OAuth ");
        boolean first = true;
        for(SortedMap.Entry<String, RequestAbstract.Parameter> oauth_param : oauth_params.entrySet()){
            if(first){
                first = false;
            }else{
                s.append(",");
            }

            try{
                s.append(oauth_param.getKey());
                s.append("=\"");
                s.append(URLEncoder.encode(oauth_param.getValue().value, "UTF-8"));
                s.append("\"");
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }

        // Set the request's authorization header
        authorizationHeader = s.toString();
    }

    public void addFileParameter(final String parameterName, final File fileHandle, final String mime){
        mStreamedParameters.put(parameterName, new FileParameter(mime, fileHandle));
    }

    public void addBytesParameter(final String parameterName, final byte[] bytes, final int length, final String mime){
        mStreamedParameters.put(parameterName, new BytesParameter(mime, bytes, length));
    }

    private okhttp3.MultipartBody buildRequestBody(){
        okhttp3.MultipartBody.Builder request_body_builder = new okhttp3.MultipartBody.Builder();

        // Add parameters
        for(SortedMap.Entry<String, Parameter> param : getParametersMap().entrySet())
            request_body_builder.addFormDataPart(param.getKey(), param.getValue().value);

        // Add file parameters
        for(SortedMap.Entry<String, StreamParameterAbstract> entry : mStreamedParameters.entrySet())
            entry.getValue().addToRequestBuilder(entry.getKey(), request_body_builder);

        request_body_builder.setType(MultipartBody.FORM);
        return request_body_builder.build();
    }

    private static abstract class StreamParameterAbstract{
        final String mime;

        StreamParameterAbstract(final String mime){
            this.mime = mime;
        }

        abstract void addToRequestBuilder(final String paramName,
                                          final okhttp3.MultipartBody.Builder builder);
    }

    private static final class FileParameter extends StreamParameterAbstract{
        final File fileHandle;

        FileParameter(final String mime, final File fileHandle){
            super(mime);
            this.fileHandle = fileHandle;
        }

        final void addToRequestBuilder(final String paramName,
                                       final okhttp3.MultipartBody.Builder builder){
            throw new RuntimeException("Stub!");
            // Below-commented line has not been tested!
            /*builder.addFormDataPart(
                    paramName, fileHandle.getName(),
                    RequestBody.create(MediaType.get(mime), fileHandle));*/
        }
    }

    private static final class BytesParameter extends StreamParameterAbstract{
        final byte[] bytes;
        final int length;

        BytesParameter(final String mime, final byte[] bytes, final int length){
            super(mime);
            this.bytes = bytes;
            this.length = length;
        }

        final void addToRequestBuilder(final String paramName,
                                       final okhttp3.MultipartBody.Builder builder){
            final StringBuilder name_b = new StringBuilder();
            name_b.append("form-data; name=");
            StringHelper.appendQuotedString(name_b, paramName);
            builder.addPart(
                    new Headers.Builder()
                            .set("Content-Disposition", name_b.toString())
                            //.set("Content-Transfer-Encoding", "base64")
                            .build(),
                    RequestBody.create(MediaType.get(mime), bytes, 0, length)
            );
        }
    }

}
