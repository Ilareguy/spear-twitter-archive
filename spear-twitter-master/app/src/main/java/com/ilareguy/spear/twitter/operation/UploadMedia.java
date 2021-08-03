package com.ilareguy.spear.twitter.operation;

import com.bluelinelabs.logansquare.LoganSquare;
import com.ilareguy.spear.BackgroundOperationExecutor;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.SpearErrorCode;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.oauth.MultipartRequest;
import com.ilareguy.spear.oauth.OAuth;
import com.ilareguy.spear.oauth.BasicRequest;
import com.ilareguy.spear.oauth.RequestAbstract;
import com.ilareguy.spear.twitter.TwitterError;
import com.ilareguy.spear.twitter.TwitterErrorCode;
import com.ilareguy.spear.twitter.data.MediaMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import androidx.annotation.Nullable;
import okhttp3.Response;

import static com.ilareguy.spear.SpearErrorCode.OK_HTTP_ERROR;

/**
 * Responsible for uploading one single media to Twitter's servers and obtaining a usable
 * media ID.
 */
public final class UploadMedia implements BackgroundOperationExecutor.Operation.Step{

    private static final int MEDIA_UPLOAD_MAX_CHUNK_SIZE = 500000; // In bytes

    private final PostTweet postTweetOperation;
    private final MediaMetadata mediaToUpload;
    private final int thisMediaIndex;
    private final int totalMediasCount;

    private long mediaId;

    public UploadMedia(final PostTweet postTweetOperation,
                       final MediaMetadata mediaToUpload,
                       final int thisMediaIndex,
                       final int totalMediasCount){
        this.postTweetOperation = postTweetOperation;
        this.mediaToUpload = mediaToUpload;
        this.thisMediaIndex = thisMediaIndex;
        this.totalMediasCount = totalMediasCount;
    }

    @Override
    public @Nullable SpearError run(final BackgroundOperationExecutor.Operation.StepHandler handler){
        // Obtain a file handle
        final File media_file = new File(mediaToUpload.getPath());
        if(!media_file.isFile()){
            // Couldn't open the file for reading
            return SpearError.build(SpearErrorCode.FILE_NOT_FOUND);
        }

        // @TODO: Check if the file is too big and resize
        // https://developer.twitter.com/en/docs/media/upload-media/uploading-media/media-best-practices

        SpearError error;

        // Execute initial command and get the new media_id
        handler.reportStatus("Preparing media...");
        mediaId = 0;
        error = uploadMediaInit(mediaToUpload, media_file);
        if(error != null) return error;

        // Execute the required APPEND commands
        handler.reportStatus("Uploading media " + String.valueOf(thisMediaIndex + 1) + " of "
                + String.valueOf(totalMediasCount) + "...");
        error = doUploadMedia(handler, mediaToUpload, media_file);
        if(error != null) return error;

        // Do the finalize command
        handler.reportStatus("Finalizing upload...");
        error = finalizeUploadMedia();
        if(error != null) return error;

        // Success! Give the new media ID back to the PostTweet object
        postTweetOperation.registerNewMediaId(mediaId);
        return null;
    }

    private @Nullable SpearError uploadMediaInit(final MediaMetadata media,
                                                 final File media_file){
        // Build request
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.POST,
                "https://upload.twitter.com/1.1/media/upload.json"
        );
        oauth_request.addParameter("command", "INIT");
        oauth_request.addParameter("total_bytes", String.valueOf(media_file.length()));
        oauth_request.addParameter("media_type", media.getMime());

        // Send & parse
        TaskResult<Response> response = null;
        try{
            // Send
            response = OAuth.getInstance().getGlobalCommunicator().sendRequest(oauth_request);

            if(!response.isSuccessful()){
                // Couldn't send the request for some reason
                return response.getError();
            }else if(!response.getObject().isSuccessful()){
                // Problem with OkHttp
                response.getObject().close();
                return SpearError.build(OK_HTTP_ERROR);
            }

            // Parse
            final UploadMedia_CommandInitResult command_result = LoganSquare.parse(
                    response.getObject().body().byteStream(),
                    UploadMedia_CommandInitResult.class
            );
            response.getObject().close();

            // Set result
            mediaId = command_result.mediaId;
            return null;
        }catch (IOException e) {
            response.getObject().close();
            return SpearError.build(e);
        }
    }

    private @Nullable SpearError doUploadMedia(final BackgroundOperationExecutor.Operation.StepHandler handler,
                                               final MediaMetadata media,
                                               final File media_file){
        final long total_bytes = media_file.length();
        final int chunks_count = (int) Math.ceil((double) total_bytes / (double) MEDIA_UPLOAD_MAX_CHUNK_SIZE);
        final byte[] bytes_to_transmit = new byte[MEDIA_UPLOAD_MAX_CHUNK_SIZE];
        final FileInputStream input_stream;

        try{
            input_stream = new FileInputStream(media_file);
            MultipartRequest oauth_request;
            long chunk_bytes_count, total_bytes_sent = 0;

            for(int current_chunk_index = 0; current_chunk_index < chunks_count; current_chunk_index++){
                chunk_bytes_count = (total_bytes - total_bytes_sent);
                if(chunk_bytes_count > MEDIA_UPLOAD_MAX_CHUNK_SIZE)
                    chunk_bytes_count = MEDIA_UPLOAD_MAX_CHUNK_SIZE;

                try{
                    total_bytes_sent += input_stream.read(bytes_to_transmit, 0, (int) chunk_bytes_count);
                }catch(IOException e){
                    // Couldn't read the file for some reason
                    return SpearError.build(e);
                }

                oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthMultipartRequest(
                        RequestAbstract.Method.POST,
                        "https://upload.twitter.com/1.1/media/upload.json"
                );
                oauth_request.addParameter("command", "APPEND", false);
                oauth_request.addParameter("media_id", String.valueOf(mediaId), false);
                oauth_request.addParameter("segment_index", String.valueOf(current_chunk_index), false);

                // Add the file data
                oauth_request.addBytesParameter(
                        "media",
                        bytes_to_transmit,
                        (int) chunk_bytes_count,
                        media.getMime());

                // Send request and check for a 2xx response code
                TaskResult<Response> response
                        = OAuth.getInstance().getGlobalCommunicator().sendRequest(oauth_request);

                if(!response.isSuccessful()){
                    // Unsuccessful response
                    return response.getError();
                }else if(!response.getObject().isSuccessful()){
                    // Problem with OkHttp
                    response.getObject().close();
                    return SpearError.build(OK_HTTP_ERROR);
                }

                // Report progress
                handler.reportProgress((float) total_bytes_sent / (float)total_bytes );
            }

            input_stream.close();
        }catch(IOException e){
            return SpearError.build(e);
        }

        return null;
    }

    private @Nullable SpearError finalizeUploadMedia(){
        // Build request
        BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                RequestAbstract.Method.POST,
                "https://upload.twitter.com/1.1/media/upload.json"
        );
        oauth_request.addParameter("command", "FINALIZE");
        oauth_request.addParameter("media_id", String.valueOf(mediaId));

        // Send & parse
        TaskResult<Response> response = null;
        try{
            // Send
            response = OAuth.getInstance().getGlobalCommunicator().sendRequest(oauth_request);

            if(!response.isSuccessful()){
                // Couldn't send the request for some reason
                return response.getError();
            }else if(!response.getObject().isSuccessful()){
                // Problem with OkHttp
                response.getObject().close();
                return SpearError.build(OK_HTTP_ERROR);
            }

            // Parse
            final UploadMedia_CommandFinalizeResult command_result = LoganSquare.parse(
                    response.getObject().body().byteStream(),
                    UploadMedia_CommandFinalizeResult.class
            );
            response.getObject().close();

            // Check if we need to wait for processing to finish
            if(command_result.processingInfo == null){
                // No need to wait
                return null;
            }

            // Wait until processing finishes
            return waitForProcessing(command_result.processingInfo.waitSeconds);

        }catch (IOException e) {
            response.getObject().close();
            return SpearError.build(e);
        }
    }

    private @Nullable SpearError waitForProcessing(final int initialWaitSecs){
        // Wait the initial time
        wait(initialWaitSecs);

        while(true){
            // Build request
            BasicRequest oauth_request = OAuth.getInstance().getGlobalAccessToken().buildOAuthRequest(
                    RequestAbstract.Method.POST,
                    "https://upload.twitter.com/1.1/media/upload.json"
            );
            oauth_request.addParameter("command", "STATUS");
            oauth_request.addParameter("media_id", String.valueOf(mediaId));

            // Send & parse
            TaskResult<Response> response = null;
            try{
                // Send
                response = OAuth.getInstance().getGlobalCommunicator().sendRequest(oauth_request);

                if(!response.isSuccessful()){
                    // Couldn't send the request for some reason
                    return response.getError();
                }else if(!response.getObject().isSuccessful()){
                    // Problem with OkHttp
                    response.getObject().close();
                    return SpearError.build(OK_HTTP_ERROR);
                }

                // Parse
                final UploadMedia_CommandFinalizeResult command_result = LoganSquare.parse(
                        response.getObject().body().byteStream(),
                        UploadMedia_CommandFinalizeResult.class
                );
                response.getObject().close();

                // Check if the media is ready
                if(command_result.processingInfo.state.equalsIgnoreCase("succeeded")){
                    return null;
                }else if(command_result.processingInfo.state.equalsIgnoreCase("failed")){
                    final StringBuilder error_message_builder = new StringBuilder();
                    error_message_builder.append("Twitter error ");
                    error_message_builder.append(command_result.processingInfo.error.code);
                    error_message_builder.append(" (");
                    error_message_builder.append(command_result.processingInfo.error.name);
                    error_message_builder.append("): ");
                    error_message_builder.append(command_result.processingInfo.error.message);
                    return TwitterError.build(TwitterErrorCode.NATIVE_TWITTER_ERROR,
                            error_message_builder.toString());
                }

                // At this point, the media is still processing
                wait(command_result.processingInfo.waitSeconds);

            }catch(IOException e){
                response.getObject().close();
                return SpearError.build(e);
            }
        }
    }

    private void wait(final int seconds){
        try{
            Thread.sleep(seconds * 1000);
        }catch (InterruptedException e){ e.printStackTrace(); }
    }

}
