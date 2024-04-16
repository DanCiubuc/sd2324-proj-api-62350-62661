package tukano.servers.java;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.rpc.context.AttributeContext.Response;

import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Users;
import tukano.clients.BlobsClientFactory;
import tukano.clients.UsersClientFactory;

public class JavaShorts implements Shorts {
    private final Map<String, Short> shorts = new HashMap<>();
    private final Map<String, List<Short>> userShorts = new HashMap<>();
    private final Map<String, List<String>> userFollowers = new HashMap<>();
    private final Map<String, List<String>> shortLikes = new HashMap<>();
    private final Map<String, String> blobs = new HashMap<>();

    private static final String SHORT_LOCATION_FORMAT = "%s/blobs/%s";

    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());

    @Override
    public Result<Short> createShort(String userId, String password) throws InterruptedException {
        Log.info("Info Received createShort : " + userId + " " + password);

        Users userService = UsersClientFactory.getClient();

        Log.info("Connected with users service");

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
        String shortId = rndId();
        Short shortObj = new Short(shortId, userId, "ola");

        // Store the object on the maps
        // TODO: integrate with hibernate
        shorts.put(shortId, shortObj);

        if (userShorts.get(userId) == null) {
            userShorts.put(userId, new ArrayList<>());
        }
        List<Short> userArchive = userShorts.get(userId);
        userArchive.add(shortObj);

        Log.info("Done Archiving short");

        String blobLocation = getBlobLocation(shortId);
        shortObj.setBlobUrl(blobLocation);

        return Result.ok(shortObj);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        Log.info("Info Received deleteShort : shortId = " + shortId + "; password = " + password);

        Short shortObj = shorts.get(shortId);

        if (shortObj == null) {
            Log.info("Short does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        String ownerId = shortObj.getOwnerId();

        // Communicate with User Service to verify if the password is valid
        Users users = UsersClientFactory.getClient();

        if (users.getUser(ownerId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // Start removing
        shorts.remove(shortId);
        blobs.remove(shortId);

        // Remove short from user archive, and update it
        List<Short> userArchive = userShorts.get(ownerId);
        userArchive.remove(shortObj);
        userShorts.put(ownerId, userArchive);

        // TODO: add removeShort to Blobs, so any videos associated with this Short gets
        // deleted from the blobs server

        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        Short shortObj = shorts.get(shortId);
        if (shortObj == null) {
            Log.info("No Short with given Id.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.ok(shortObj);
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        Users users = UsersClientFactory.getClient();
        Log.info("id:" + " " + userId);
        Log.info(users.getUser(userId, "").error().toString());
        if (users.getUser(userId, "").error().equals(ErrorCode.NOT_FOUND)) {
            Log.info("User doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        userShorts.putIfAbsent(userId, new ArrayList<>());
        List<String> result = new ArrayList<String>();
        userShorts.get(userId).forEach((s) -> result.add(s.getShortId()));
        return Result.ok(result);
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        Users users = UsersClientFactory.getClient();

        if (users.getUser(userId1, password).error().equals(ErrorCode.NOT_FOUND)
                || users.getUser(userId2, password).error().equals(ErrorCode.NOT_FOUND)) {
            Log.info("One of the users doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        if (users.getUser(userId1, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // if the user is not in the map
        userFollowers.putIfAbsent(userId1, new ArrayList<String>());

        if (isFollowing) {
            List<String> user1Followers = userFollowers.get(userId1);
            user1Followers.add(userId2);
            userFollowers.put(userId1, user1Followers);
        } else {
            List<String> user1Followers = userFollowers.get(userId1);
            user1Followers.remove(userId2);
            userFollowers.put(userId1, user1Followers);
        }

        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Users users = UsersClientFactory.getClient();

        ErrorCode error = users.getUser(userId, password).error();

        if (error.equals(ErrorCode.NOT_FOUND)) {
            Log.info("User doesn't exist.");
        }

        if (error.equals(ErrorCode.FORBIDDEN)) {
            Log.info("Password incorect.");
        }

        List<String> flws = userFollowers.get(userId);

        List<String> result = (flws == null) ? (new ArrayList<String>()) : flws;

        return Result.ok(result);
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        Users users = UsersClientFactory.getClient();

        if (users.getUser(userId, password).error().equals(ErrorCode.NOT_FOUND)) {
            Log.info("User doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        if (!shorts.containsKey(shortId)) {
            Log.info("Short doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        shortLikes.putIfAbsent(shortId, new ArrayList<String>());

        if (shortLikes.get(shortId).contains(userId) && isLiked) {
            Log.info("Short already liked by user.");
            return Result.error(ErrorCode.CONFLICT);
        }

        if (users.getUser(userId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Password is incorect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        if (isLiked) {
            shortLikes.get(shortId).add(userId);
        }

        if (!isLiked && !shortLikes.get(shortId).contains(userId)) {
            Log.info("User didn't have like in this post.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        if (!isLiked) {
            shortLikes.get(shortId).remove(userId);
        }

        return Result.ok();

    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        if (!shorts.containsKey(shortId)) {
            Log.info("Short doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Users users = UsersClientFactory.getClient();

        String userId = shorts.get(shortId).getOwnerId();
        if (users.getUser(userId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorect Password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<String> likesInPost = shortLikes.get(shortId);

        List<String> result = (likesInPost == null) ? (new ArrayList<String>()) : likesInPost;

        return Result.ok(result);

    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Users users = UsersClientFactory.getClient();

        if (users.getUser(userId, password).error().equals(ErrorCode.NOT_FOUND)) {
            Log.info("User doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        if (users.getUser(userId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<String> followingUsers = new ArrayList<String>();
        List<String> shortsInFeed = new ArrayList<String>();

        for (Map.Entry<String, List<String>> entry : userFollowers.entrySet()) {
            if (entry.getValue().contains(userId))
                followingUsers.add(entry.getKey());
        }

        for (Map.Entry<String, List<Short>> entry : userShorts.entrySet()) {
            if (followingUsers.contains(entry.getKey())) {
                for (Short el : entry.getValue()) {
                    shortsInFeed.add(el.getShortId());
                }
            }

        }

        return Result.ok(shortsInFeed);

    }

    @Override
    public Result<Void> verifyBlobId(String blobId) {
        if (!blobs.containsValue(blobId)) {
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.ok();
    }

    private String getBlobLocation(String shortId) throws InterruptedException {
        URI blobUri = BlobsClientFactory.getUri();
        String blobId = rndId();

        blobs.put(shortId, blobId);

        String blobLocation = String.format(SHORT_LOCATION_FORMAT, blobUri, blobId);

        return blobLocation;
    }

    // TODO: meter isto no utils
    private String rndId() {
        String uuid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16));
        String uuid16digits = uuid.substring(uuid.length() - 16);

        return uuid16digits;
    }

}
