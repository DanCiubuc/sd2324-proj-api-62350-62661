package tukano.servers.java;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class JavaBlobs implements Blobs {
    private final Map<String, byte[]> blobs = new HashMap<>();

    private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());
    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Log.info("Info Received upload : blobId = " + blobId);

        // if a blobId exists but bytes do not match
        if(blobs.get(blobId) != null && blobs.get(blobId) != bytes) {
            Log.info("Blob already exists but bytes do not match.");
            return Result.error( Result.ErrorCode.CONFLICT);
        }
        // if the blobId is not valid
        // return Forbidden

        blobs.put(blobId, bytes);
        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {
        Log.info("Info Received download : blobId = " + blobId);

        //Checking if blob id exists
        if(blobs.get(blobId) == null) {
            Log.info("Blob does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }

        return Result.ok(blobs.get(blobId));
    }

}
