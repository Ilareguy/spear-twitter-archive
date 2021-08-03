package com.ilareguy.spear.oauth;

import com.ilareguy.spear.util.HmacSha1Signature;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.SortedMap;

public abstract class SignatureGenerator{

    /**
     * Returns a String containing the Hmac-Sha1-encoded signature for the given request.
     * This can be used to get a signature *before* you have a valid Access Token available
     * for use; i.e., when you want to request an Unauthorized Request Token.
     */
    public static String getSignatureForRequest(final AuthenticationRequest authentication_request){
        // Get the base string
        String base_string = getBaseString(authentication_request.getMethod(),
                authentication_request.getUrl(), authentication_request.getParametersMap());

        // Get the key for the HMAC-SHA1 algorithm
        String key = getConcatenatedKey(authentication_request.getConsumerKey().getSecret(), "");

        // Encode it using HMAC-SHA1 algorithm
        return HmacSha1Signature.calculateRFC2104HMAC(base_string, key);
    }

    public static String getSignatureForRequest(final BasicRequest request){
        // Get the base string
        String base_string = getBaseString(request.getMethod(), request.getUrl(), request.getParametersMap());

        // Get the key for the HMAC-SHA1 algorithm
        String key = getConcatenatedKey(request.getConsumerKey().getSecret(), request.getAccessToken().getSecret());

        // Encode it using HMAC-SHA1 algorithm
        return HmacSha1Signature.calculateRFC2104HMAC(base_string, key);
    }

    public static String getSignature(final BasicRequest.Method requestMethod,
                                      final String url,
                                      final ConsumerKey consumerKey,
                                      final AccessToken accessToken,
                                      final SortedMap<String, RequestAbstract.Parameter> oauthParams){
        // Get the base string
        String base_string = getBaseString(requestMethod, url, oauthParams);

        // Get the key for the HMAC-SHA1 algorithm
        String key = getConcatenatedKey(consumerKey.getSecret(), accessToken.getSecret());

        // Encode it using HMAC-SHA1 algorithm
        return HmacSha1Signature.calculateRFC2104HMAC(base_string, key);
    }

    public static String getBodyHash(final ConsumerKey consumerKey,
                                     final AccessToken accessToken,
                                     final String bodyString){
        // Get the key for the HMAC-SHA1 algorithm
        String key = getConcatenatedKey(consumerKey.getSecret(), accessToken.getSecret());

        // Encode it using HMAC-SHA1 algorithm
        return HmacSha1Signature.calculateRFC2104HMAC(bodyString, key);
    }

    /**
     * Generates the base string for a signature.
     */
    private static String getBaseString(final BasicRequest.Method requestMethod,
                                        final String url,
                                        final SortedMap<String, RequestAbstract.Parameter> unsortedParameters){
        /*
         * STEP 1: Normalize Request Parameters
         * Process described in paragraph 9.1.1 of
         * https://oauth.net/core/1.0/#signing_process
         */
        String normalized_parameters = getNormalizedParameters(unsortedParameters);

        /*
         * STEP 2: Construct Request URL
         * Process described in paragraph 9.1.2 of
         * https://oauth.net/core/1.0/#signing_process
         */
        // If the URL was supplied properly, it shouldn't contain any extra parameter
        // or the default ports (80 or 443). Just make sure it's all in lowercase.
        String normalized_url = url.toLowerCase();

        /*
         * STEP 3: Concatenate Request Elements
         * Process described in paragraph 9.1.3 of
         * https://oauth.net/core/1.0/#signing_process
         */

        // "Each item is encoded and separated by an ‘&’ character (ASCII code 38), even if empty."
        // See https://oauth.net/core/1.0/#encoding_parameters
        return encodeForOAuth(RequestAbstract.getMethodString(requestMethod)) + '&'
                + encodeForOAuth(normalized_url) + '&' + encodeForOAuth(normalized_parameters);
    }

    private static String getNormalizedRequestParameters(final RequestAbstract request){
        return getNormalizedParameters(request.getParametersMap());
    }

    private static String getNormalizedParameters(final SortedMap<String, RequestAbstract.Parameter> unsortedParameters){
        // TODO: Actually sort by value if two or more parameters share the same name.
        // TODO (cont'd): Provide a custom Comparator object that takes value in consideration to the SortedMap constructor in OAuthRequest.java.

        /*
         * The parameters in an OAuthRequest object are built upon a SortedMap
         * object. According to the documentation, the "order is reflected when
         * iterating over the sorted map's collection".
         */

        // Sort by name and concatenate into a single String
        StringBuilder str_builder = new StringBuilder();

        boolean first_entry = true;

        for (Map.Entry<String, RequestAbstract.Parameter> entry : unsortedParameters.entrySet()) {
            if(!entry.getValue().useInSigning) continue;

            if (first_entry)
                first_entry = false;
            else
                str_builder.append("&");

            str_builder.append(entry.getKey());
            str_builder.append('=');
            str_builder.append(encodeForOAuth(entry.getValue().value));
        }

        return str_builder.toString();
    }

    /**
     * Returns a concatenated String that can be used as the key to encode the request
     * signature using the Hmac-Sha1 algorithm.
     * See paragraph 9.2 of https://oauth.net/core/1.0/#auth_step1
     */
    private static String getConcatenatedKey(final String consumer_secret,
                                             final String access_token_secret){
        return (encodeForOAuth(consumer_secret) + '&' + encodeForOAuth(access_token_secret));
    }

    /**
     * Percent-encodes the given String and returns the result.
     * Do not use this method outside of an OAuth scope as the OAuth
     * expects a slightly different encoding than regular percent-encoding!
     * See:
     * https://android.googlesource.com/platform/external/oauth/+/ics-mr1/core/src/main/java/net/oauth/OAuth.java
     */
    private static String encodeForOAuth(String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

}
