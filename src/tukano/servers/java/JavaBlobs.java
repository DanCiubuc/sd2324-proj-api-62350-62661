package tukano.servers.java;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.clients.ShortsClientFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        if (blobs.get(blobId) != null && blobs.get(blobId) != bytes) {
            Log.info("Blob already exists but bytes do not match.");
            return Result.error(Result.ErrorCode.CONFLICT);
        }

        Shorts shorts = ShortsClientFactory.getClient();
        if (!shorts.verifyBlobId(blobId).isOK()) {
            Log.info("Invalid blobId");
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }

        String fileName = blobId; // Or any desired naming convention
        Path filePath = Paths.get(fileName);
        try {
            Files.write(filePath, bytes);
            Log.info("Successfully wrote bytes to file: " + fileName);
        } catch (IOException e) {
            Log.info("Failed to write bytes to file: " + fileName);
            return Result.error(Result.ErrorCode.INTERNAL_ERROR); // Or specific exception handling
        }
        blobs.put(blobId, bytes);

        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {
        Log.info("Info Received download : blobId = " + blobId);

        // Checking if blob id exists
        if (blobs.get(blobId) == null) {
            Log.info("Blob does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        String fileName = blobId;
        Path filePath = Paths.get(fileName);
        // Read file bytes
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return Result.ok(bytes);
        } catch (IOException e) {
            // Handle potential exceptions like file not found
            if (e instanceof NoSuchFileException) {
                Log.info("Blob does not exist in file: " + fileName);
                return Result.error(Result.ErrorCode.NOT_FOUND);
            } else {
                Log.info("Failed to read file: " + fileName);
                return Result.error(Result.ErrorCode.INTERNAL_ERROR); // Or specific handling
            }
        }

    }

    @Override
    public Result<Void> remove(String blobId) {
        Log.info("Info Received remove : blobId = " + blobId);

        // Checking if blob id exists
        if (blobs.get(blobId) == null) {
            Log.info("Blob does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Construct the file path based on blobId
        String fileName = blobId;
        Path filePath = Paths.get(fileName);

        // Try to delete the file
        try {
            Files.delete(filePath);
            Log.info("Successfully deleted file: " + fileName);
        } catch (IOException e) {
            // Handle potential exceptions like file not found
            if (e instanceof NoSuchFileException) {
                Log.info("Blob does not exist in file: " + fileName);
            } else {
                Log.info("Failed to delete file: " + fileName);
                return Result.error(Result.ErrorCode.INTERNAL_ERROR);
            }
        }

        blobs.remove(blobId);

        return Result.ok();
    }

}
