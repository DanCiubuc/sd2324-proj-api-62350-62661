package tukano.servers.java;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.servers.rest.RestUsersResource;

public class JavaShorts implements Shorts {
    // TODO: get instance of UserResource and BlobsResource

    private final Map<String, Short> shorts = new HashMap<>();
    private final Map<String, Short> userShorts = new HashMap<>();
    private final Map<String, List<User>> followers = new HashMap<>();

    // private final RestUsersResource userResource = ;

    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());

    @Override
    public Result<Short> createShort(String userId, String password) {
        // TODO Auto-generated method stub
        // Shorts server verifies user/pwd and
        // returns location for blob

        // #1) get user, in order to verify that the userId is valid
        // #2) create the short and put it into map
        // #3) return location of the blob where the short was stored:
        // http://{name_of_the_blob}:8080/rest/blobs/{blob_id}

        throw new UnsupportedOperationException("Unimplemented method 'createShort'");
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShort'");
    }

    @Override
    public Result<Short> getShort(String shortId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShort'");
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
