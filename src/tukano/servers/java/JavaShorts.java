package tukano.servers.java;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import tukano.api.Follow;
import tukano.api.Likes;
import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Users;
import tukano.clients.BlobsClientFactory;
import tukano.clients.UsersClientFactory;
import tukano.persistence.Hibernate;

public class JavaShorts implements Shorts {
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
        Short shortObj = new Short(shortId, userId, "BLOB_URL");

        String blobLocation = getBlobLocation(shortId);
        shortObj.setBlobUrl(blobLocation);

        // Store the object on the maps
        // TODO: integrate with hibernate
        Hibernate.getInstance().persist(shortObj);
        return Result.ok(shortObj);
    }

    @Override
    public Result<Short> getShort(String shortId) {
        List<Short> shorts_list = getShortHibernate(shortId);
        if (shorts_list.isEmpty()) {
            Log.info("No Short with given Id.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        Short shortObj = shorts_list.get(0);
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
        List<Short> shorts = Hibernate.getInstance().sql(String.format("SELECT * FROM Short s WHERE s.ownerId LIKE '%%%s%%'", userId), Short.class);
        List<String> result = new ArrayList<String>();
        for(Short s : shorts) {
            result.add(s.getShortId());
        }
        return Result.ok(result);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        Log.info("Info Received deleteShort : shortId = " + shortId + "; password = " + password);
        List<Short> shorts_list = getShortHibernate(shortId);

        if (shorts_list.isEmpty()) {
            Log.info("Short does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Short shortObj = shorts_list.get(0);

        String ownerId = shortObj.getOwnerId();

        // Communicate with User Service to verify if the password is valid
        Users users = UsersClientFactory.getClient();

        if (users.getUser(ownerId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // Start removing
        Hibernate.getInstance().delete(shortObj);

        // TODO: add removeShort to Blobs, so any videos associated with this Short gets
        // deleted from the blobs server
        //blobs.remove(shortId);

        return Result.ok();
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        Users users = UsersClientFactory.getClient();

        ErrorCode error1 = users.searchUsers(userId1).error();
        ErrorCode error2 = users.searchUsers(userId2).error();
        if(error1.equals(ErrorCode.BAD_REQUEST) || error2.equals(ErrorCode.BAD_REQUEST)) {
            Log.info("One of the users doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        if (users.getUser(userId1, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        List<Follow> list_follow = getFollower(userId1, userId2);
        if (isFollowing) {
            if(!list_follow.isEmpty()) {
                Log.info("Already Following User.");
                return Result.error(ErrorCode.CONFLICT);
            }
            Follow follow = new Follow(userId2, userId1);
            Hibernate.getInstance().persist(follow);
        } else {
            if(list_follow.isEmpty()) {
                Log.info("Not Following User.");
                return Result.error(ErrorCode.CONFLICT);
            }
            Follow f = list_follow.get(0);
            Hibernate.getInstance().delete(f);
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

        List<String> result = getFollowers(userId);

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

        List<String> shortsInFeed = new ArrayList<String>();
        List<Short> shorts = getShortFromUser(userId);
        for (Short el : shorts) {
            shortsInFeed.add(el.getShortId());
        }

        return Result.ok(shortsInFeed);
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        Users users = UsersClientFactory.getClient();

        if (users.getUser(userId, password).error().equals(ErrorCode.NOT_FOUND)) {
            Log.info("User doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        List<Short> shorts_list = getShortHibernate(shortId);

        if (shorts_list.isEmpty()) {
            Log.info("Short doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        if (users.getUser(userId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Password is incorect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        List<Likes> like_list = getLikes(shortId, userId);

        if (isLiked) {
            if (!like_list.isEmpty()) {
                Log.info("Short already liked by user.");
                return Result.error(ErrorCode.CONFLICT);
            }

            Likes like = new Likes(shortId, userId);
            Short shor = shorts_list.get(0);
            int likes = shor.getTotalLikes();
            shor.setTotalLikes(likes + 1);
            Hibernate.getInstance().update(shor);
            Hibernate.getInstance().persist(like);
        } else {
            if (like_list.isEmpty()) {
                Log.info("User didn't have like in this post.");
                return Result.error(ErrorCode.BAD_REQUEST);
            }
            Likes l = like_list.get(0);
            Short shor = shorts_list.get(0);
            int likes = shor.getTotalLikes();
            shor.setTotalLikes(likes - 1);
            Hibernate.getInstance().update(shor);
            Hibernate.getInstance().delete(l);
        }
        return Result.ok();
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        List<Short> shorts_list = getShortHibernate(shortId);
        if (shorts_list.isEmpty()) {
            Log.info("Short doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Users users = UsersClientFactory.getClient();

        String userId = shorts_list.get(0).getOwnerId();
        if (users.getUser(userId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorect Password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<String> result = getLikesFromShort(shortId);

        return Result.ok(result);
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

    private List<Short> getShortHibernate(String shortId) {
        return Hibernate.getInstance().sql(String.format("SELECT * FROM Short s WHERE s.shortId LIKE '%%%s%%'", shortId), Short.class);
    }

    private List<Short> getShortFromUser(String follower) {
        return Hibernate.getInstance().sql(String.format("SELECT s.* FROM Short s LEFT JOIN Follow f ON f.following = s.ownerId WHERE f.followedBy LIKE '%%%s%%' " +
                "UNION " +
                "SELECT m.* FROM Short m WHERE m.ownerId LIKE '" + follower + "' ORDER BY s.timestamp DESC", follower), Short.class);
    }

    private List<Follow> getFollower(String userId1, String userId2) {
        return Hibernate.getInstance().sql(String.format("SELECT * FROM Follow WHERE following LIKE '" + userId2 + "' AND followedBy LIKE '" + userId1 + "'"), Follow.class);
    }

    private List<String> getFollowers(String follower) {
        List<String> fol_list = new ArrayList<>();
        List<Follow> followers =  Hibernate.getInstance().sql(String.format("SELECT * FROM Follow f WHERE f.following LIKE '%%%s%%'", follower), Follow.class);
        for(Follow f : followers) {
           String follower_id = f.getFollowedBy();
            fol_list.add(follower_id);
        }
        return fol_list;
    }

    private List<Likes> getLikes(String shortId, String userId) {
        return Hibernate.getInstance().sql(String.format("SELECT * FROM Likes WHERE shortId LIKE '" + shortId + "' AND userId LIKE '" + userId + "'"), Likes.class);
    }
    private List<String> getLikesFromShort(String shortId) {
        List<String> likes = new ArrayList<>();
        List<Likes> list = Hibernate.getInstance().sql(String.format("SELECT * FROM Likes l WHERE l.shortId LIKE '%%%s%%'", shortId), Likes.class);
        for(Likes l : list) {
            likes.add(l.getUserId());
        }
        return likes;
    }

}
