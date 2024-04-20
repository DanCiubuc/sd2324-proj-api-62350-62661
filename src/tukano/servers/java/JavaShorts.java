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
import tukano.api.java.Blobs;
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
    private static final String DELIMITER = "/";
    private static final String ADDRESS_ID_SEPARATOR = "/blobs";

    private static final int SHORT_ID_SIZE = 16;

    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());

    @Override
    public Result<Short> createShort(String userId, String password) {
        Log.info("Info Received createShort : " + userId + " " + password);

        Users userService = UsersClientFactory.getClient();

        Log.info("Connected with users service");

        // Verifies if the user exists in the user server
        if (!userService.getUser(userId, password).isOK()) {
            Log.info("No user with provided id.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        User user = userService.getUser(userId, password).value();

        // Checks if the provided password is correct
        if (!user.pwd().equals(password)) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // Create the short object
        String shortId = rndId();
        Short shortObj = new Short(shortId, userId, "BLOB_URL");

        // Generate the blob location for the short
        String blobLocation = generateBlobLocation(shortId);
        shortObj.setBlobUrl(blobLocation);

        // Store the object on the maps
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

        // Checks if user exists
        if (users.getUser(userId, "").error().equals(ErrorCode.NOT_FOUND)) {
            Log.info("User doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // Get the list of shorts owned by this user
        List<Short> shorts = Hibernate.getInstance()
                .sql(String.format("SELECT * FROM Short s WHERE s.ownerId LIKE '%%%s%%'", userId), Short.class);

        // and stores them in an array, to be encapsulated in the Response
        List<String> result = new ArrayList<String>();
        for (Short s : shorts) {
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
        // Checking if the password is correct
        if (users.getUser(ownerId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        String blobUrl = shortObj.getBlobUrl();
        // finds the index where ADDRESS_ID_SEPARATOR starts
        int lastSlashIndex = blobUrl.lastIndexOf("/", blobUrl.lastIndexOf("blobs/") - 1);
        // the string that starts at 0 and ends at lastStashIndex correspondes with the
        // address of the blob server where the short is stored
        String blobsAddress = blobUrl.substring(0, lastSlashIndex);
        // the rest of the string, after the ADDRESS_ID_SEPARATOR, corresponds with the
        // id of the blob
        String blobId = blobUrl.substring(blobUrl.lastIndexOf("/") + 1);

        // Communicate with Blob Service that stores this shorts video
        Blobs blobsService = BlobsClientFactory.getClient(blobsAddress);

        // Sends request to remove from the blobs server
        blobsService.remove(blobId);

        // Start removing
        Hibernate.getInstance().delete(shortObj);

        return Result.ok();
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        // checks if the user related information is valid
        Users users = UsersClientFactory.getClient();

        ErrorCode error1 = users.getUser(userId1, password).error();
        ErrorCode error2 = users.getUser(userId2, "").error();
        // Checkin if the users exist
        if (error1.equals(ErrorCode.NOT_FOUND) || error2.equals(ErrorCode.NOT_FOUND)) {
            Log.info("One of the users doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        // Checking if the password is correct
        if (users.getUser(userId1, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // Retrieves the follow relation between user1 and user2, if it exists
        List<Follow> list_follow = getFollower(userId1, userId2);
        if (isFollowing) {
            // deals with the case where the user follows another
            if (!list_follow.isEmpty()) {
                Log.info("Already Following User.");
                return Result.error(ErrorCode.CONFLICT);
            }
            Follow follow = new Follow(userId2, userId1);
            // Adding Follow
            Hibernate.getInstance().persist(follow);

        } else {
            // deals with the case where the user unfollows another
            if (!list_follow.isEmpty()) {
                Follow f = list_follow.get(0);
                // Removing Follow
                Hibernate.getInstance().delete(f);
            }

        }
        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        // checks if the information provided is valid
        Users users = UsersClientFactory.getClient();

        ErrorCode error = users.getUser(userId, password).error();
        // Checking if user exists
        if (error.equals(ErrorCode.NOT_FOUND)) {
            Log.info("User doesn't exist.");
        }
        // Checking of the password is correct
        if (error.equals(ErrorCode.FORBIDDEN)) {
            Log.info("Password incorect.");
        }

        List<String> result = getFollowers(userId);

        return Result.ok(result);
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        // checks if the information provided is valid
        Users users = UsersClientFactory.getClient();
        // Checking if the user exists
        if (users.getUser(userId, password).error().equals(ErrorCode.NOT_FOUND)) {
            Log.info("User doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        // Checking if the password is correct
        if (users.getUser(userId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // stores the shortIds that are in this users feed
        List<String> shortsInFeed = new ArrayList<String>();
        List<Short> shorts = getShortFromUser(userId);
        for (Short el : shorts) {
            // Adding shorts to Feed
            shortsInFeed.add(el.getShortId());
        }

        return Result.ok(shortsInFeed);
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        // verifies if the user related information is valid
        Users users = UsersClientFactory.getClient();
        // Checking if the user exists
        if (users.getUser(userId, password).error().equals(ErrorCode.NOT_FOUND)) {
            Log.info("User doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        // Checking if the password is correct
        if (users.getUser(userId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Password is incorect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<Short> shorts_list = getShortHibernate(shortId);

        // checks if short exists
        if (shorts_list.isEmpty()) {
            Log.info("Short doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // retrieves the Like object, that maps the relation between a user and a short
        List<Likes> like_list = getLikes(shortId, userId);

        // case where a like is added
        if (isLiked) {
            if (!like_list.isEmpty()) {
                Log.info("Short already liked by user.");
                return Result.error(ErrorCode.CONFLICT);
            }

            Likes like = new Likes(shortId, userId);
            Short shor = shorts_list.get(0);
            int likes = shor.getTotalLikes();
            shor.setTotalLikes(likes + 1);
            // Updating short's total likes
            Hibernate.getInstance().update(shor);
            // Adding Like
            Hibernate.getInstance().persist(like);
        } else {
            // case where a like is removed
            if (like_list.isEmpty()) {
                Log.info("User didn't have like in this post.");
                return Result.error(ErrorCode.BAD_REQUEST);
            }

            Likes l = like_list.get(0);
            Short shor = shorts_list.get(0);
            Log.info("Before removing like" + shor.toString());

            int likes = shor.getTotalLikes();
            shor.setTotalLikes(likes - 1);
            // Updating short's total likes
            Hibernate.getInstance().update(shor);
            // Adding Like
            Hibernate.getInstance().delete(l);
            Log.info("After removing like" + shor.toString());
        }
        return Result.ok();
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        List<Short> shorts_list = getShortHibernate(shortId);
        // checks if the short exists
        if (shorts_list.isEmpty()) {
            Log.info("Short doesn't exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Users users = UsersClientFactory.getClient();

        // check if the provided password is correct
        String userId = shorts_list.get(0).getOwnerId();
        if (users.getUser(userId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorect Password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<String> result = getLikesFromShort(shortId);

        return Result.ok(result);
    }

    public Result<List<String>> likeHistory(String userId, String password) {
        // checks if the user provided information is valid
        Users users = UsersClientFactory.getClient();
        // Checking if the user exists
        if (users.getUser(userId, password).error().equals(ErrorCode.NOT_FOUND)) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        // Checking if the password is correct
        if (users.getUser(userId, password).error().equals(ErrorCode.FORBIDDEN)) {
            Log.info("Incorect Password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // retrieves and stores the shortIds in an array that will be encapsulated in
        // the response
        List<Likes> liked = getLikedHistory(userId);
        List<String> likedShorts = new ArrayList<>();

        for (Likes likes : liked) {
            // Adding Likes to Likes History
            likedShorts.add(likes.getShortId());
        }

        Log.info(likedShorts.toString());

        return Result.ok(likedShorts);
    }

    @Override
    public Result<Void> verifyBlobId(String blobId) {
        // Verifies if the blobId is valid
        if (!blobs.containsValue(blobId)) {
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.ok();
    }

    /**
     * 
     * @param shortId
     * @return
     */
    private String generateBlobLocation(String shortId) {
        try {
            URI blobUri;
            // Retrieves URI from Blobs Factory
            blobUri = BlobsClientFactory.getUri();
            // Generates blobId
            String blobId = rndId();
            blobs.put(shortId, blobId);
            // Creates the blob URL
            String blobLocation = String.format(SHORT_LOCATION_FORMAT, blobUri, blobId);
            return blobLocation;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 
     * @return a random uuid with lenght SHORT_ID_SIZE
     */
    private String rndId() {
        String uuid = String.format("%040d",
                new BigInteger(UUID.randomUUID().toString().replace("-", ""), SHORT_ID_SIZE));
        String uuid16digits = uuid.substring(uuid.length() - SHORT_ID_SIZE);

        return uuid16digits;
    }

    /**
     * 
     * @param shortId identifier of the short
     * @return List of the Short instances that matches the given shortId
     */
    private List<Short> getShortHibernate(String shortId) {
        return Hibernate.getInstance()
                .sql(String.format("SELECT * FROM Short s WHERE s.shortId LIKE '%%%s%%'", shortId), Short.class);
    }

    /**
     * 
     * @param follower identifier of the User
     * @return List of the Shorts from the Users that User follower follows
     */
    private List<Short> getShortFromUser(String follower) {
        return Hibernate.getInstance().sql(String.format(
                "SELECT s.* FROM Short s LEFT JOIN Follow f ON f.following = s.ownerId WHERE f.followedBy LIKE '%%%s%%' "
                        +
                        "UNION " +
                        "SELECT m.* FROM Short m WHERE m.ownerId LIKE '" + follower + "' ORDER BY s.timestamp DESC",
                follower), Short.class);
    }

    /**
     * 
     * @param userId1 - user that is followed
     * @param userId2 - user that is following
     * @return List of instances of Follow object that stores the following relation
     *         between user1 and user2
     */
    private List<Follow> getFollower(String userId1, String userId2) {
        return Hibernate.getInstance().sql(String.format(
                "SELECT * FROM Follow WHERE following LIKE '" + userId2 + "' AND followedBy LIKE '" + userId1 + "'"),
                Follow.class);
    }

    /**
     * 
     * @param follower identifier of the user
     * @return List of userIds of the users that the given user (follower) follows
     */
    private List<String> getFollowers(String follower) {
        List<String> fol_list = new ArrayList<>();
        List<Follow> followers = Hibernate.getInstance()
                .sql(String.format("SELECT * FROM Follow f WHERE f.following LIKE '%%%s%%'", follower), Follow.class);
        for (Follow f : followers) {
            String follower_id = f.getFollowedBy();
            fol_list.add(follower_id);
        }
        return fol_list;
    }

    /**
     * 
     * @param shortId identifer of the short that was liked
     * @param userId  identifier of the user that gave the like
     * @return List with the instance of the Like
     */
    private List<Likes> getLikes(String shortId, String userId) {
        return Hibernate.getInstance()
                .sql(String.format(
                        "SELECT * FROM Likes WHERE shortId LIKE '" + shortId + "' AND userId LIKE '" + userId + "'"),
                        Likes.class);
    }

    /**
     * 
     * @param shortId identifier of the short
     * @return List of the ids of the users that liked the short
     */
    private List<String> getLikesFromShort(String shortId) {
        List<String> likes = new ArrayList<>();
        List<Likes> list = Hibernate.getInstance()
                .sql(String.format("SELECT * FROM Likes l WHERE l.shortId LIKE '%%%s%%'", shortId), Likes.class);
        for (Likes l : list) {
            likes.add(l.getUserId());
        }
        return likes;
    }

    /**
     * 
     * @param userId identifier of the user
     * @return list of the likes given by this user
     */
    private List<Likes> getLikedHistory(String userId) {
        return Hibernate.getInstance()
                .sql(String.format(
                        "SELECT * FROM Likes WHERE userId LIKE '" + userId + "'"),
                        Likes.class);
    }

}
