package tukano.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.rest.RestShorts;

import java.net.URI;
import java.util.List;

public class RestShortsClient extends RestClient implements Shorts {
    final WebTarget target;

    public RestShortsClient(URI serverURI) {
        super(serverURI);

        target = client.target(serverURI).path(RestShorts.PATH);
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        return super.reTry(() -> clt_createShort(userId, password));
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        return super.reTry(() -> clt_deleteShort(shortId, password));
    }

    @Override
    public Result<Short> getShort(String shortId) {
        return super.reTry(() -> clt_getShort(shortId));
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        return super.reTry(() -> clt_getShorts(userId));
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        throw new UnsupportedOperationException("Unimplemented method 'follow'");
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        throw new UnsupportedOperationException("Unimplemented method 'followers'");
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        throw new UnsupportedOperationException("Unimplemented method 'like'");
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        throw new UnsupportedOperationException("Unimplemented method 'likes'");
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        throw new UnsupportedOperationException("Unimplemented method 'getFeed'");
    }

    // Private helper methods

    private Result<Short> clt_createShort(String userId, String password) {
        return super.toJavaResult(
                target.path(userId)
                        .queryParam(RestShorts.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity("", MediaType.APPLICATION_JSON)),
                Short.class);
    }

    private Result<Void> clt_deleteShort(String shortId, String password) {
        return super.toJavaResult(
                target.path(shortId)
                        .queryParam(RestShorts.PWD, password)
                        .request()
                        .delete(),
                Void.class);
    }

    private Result<Short> clt_getShort(String shortId) {
        return super.toJavaResult(
                target.path(shortId)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(),
                Short.class);
    }

    private Result<List<String>> clt_getShorts(String userId) {
        return super.toJavaResultForList(
                target.queryParam(RestShorts.USER_ID, userId)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(),
                String.class);
    }
}
