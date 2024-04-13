package tukano.servers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Users;
import tukano.clients.UsersClientFactory;
import tukano.servers.rest.RestUsersResource;

public class JavaShorts implements Shorts {
    // TODO: get instance of UserResource and BlobsResource

    private final Map<String, Short> shorts = new HashMap<>();
    private final Map<String, List<Short>> userShorts = new HashMap<>();
    private final Map<String, List<User>> followers = new HashMap<>();

    // Each short has a number of blobs
    // So each blobId -> short
    private final Map<String, Short> blobs = new HashMap<>();

    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());

    @Override
    public Result<Short> createShort(String userId, String password) throws InterruptedException {
        Log.info("Info Received createShort : " + userId + " " + password);

        Users userService = UsersClientFactory.getClient();

        Log.info("Connected with userService");

        if (!userService.getUser(userId, password).isOK()) {
            Log.info("No user with provided id.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        User user = userService.getUser(userId, password).value();

        if (!user.pwd().equals(password)) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // Create the short object
        String shortId = UUID.randomUUID().toString();
        Short shortObj = new Short(shortId, userId, "ola");

        // Store the object on the maps
        // TODO: integrate with hibernate
        shorts.put(shortId, shortObj);

        if (userShorts.get(userId) == null) {
            userShorts.put(userId, new ArrayList<>());
        }
        List<Short> userArchive = userShorts.get(userId);
        userArchive.add(shortObj);

        // TODO: add other checks

        // TODO: generate and return blob connection

        // #1) get user, in order to verify that the userId is valid
        // #2) create the short and put it into map
        // #3) return location of the blob where the short was stored:
        // http://{name_of_the_blob}:8080/rest/blobs/{blob_id}

        return Result.ok(shortObj);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShort'");
    }

    @Override
    public Result<Short> getShort(String shortId) {
        // TODO Auto-generated method stub
        Short shortObj = shorts.get(shortId);
        if (shortObj == null) {
            Log.info("No Short with given Id.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.ok(shortObj);
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShorts'");
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'follow'");
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'followers'");
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'like'");
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'likes'");
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFeed'");
    }

}
