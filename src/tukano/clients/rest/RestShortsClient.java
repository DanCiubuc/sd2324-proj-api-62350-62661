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
        return super.reTry(() -> clt_follow(userId1, userId2, isFollowing, password));
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        return super.reTry(() -> clt_followers(userId, password));
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        return super.reTry(() -> clt_like(shortId, userId, isLiked, password));
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        return super.reTry(() -> clt_likes(shortId, password));
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        return super.reTry(() -> clt_getFeed(userId, password));
    }

    @Override
    public Result<Void> verifyBlobId(String blobId) {
        return super.reTry(() -> clt_verifyBlobId(blobId));
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
                target.path(userId + RestShorts.SHORTS)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(),
                String.class);
    }

    private Result<Void> clt_verifyBlobId(String blobId) {
        return super.toJavaResult(
                target.path(blobId + "/verify")
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity("", MediaType.APPLICATION_JSON)),
                null);
    }

    private Result<Void> clt_follow(String userId1, String userId2, boolean isFollowing, String password) {
        System.out.println(target.path(userId1 + "/" + userId2 + RestShorts.FOLLOWERS)
                .queryParam(RestShorts.PWD, password).toString());
        return super.toJavaResult(
                target.path(userId1 + "/" + userId2 + RestShorts.FOLLOWERS)
                        .queryParam(RestShorts.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(isFollowing, MediaType.APPLICATION_JSON)));
    }

    private Result<List<String>> clt_followers(String userId, String password) {
        return super.toJavaResultForList(
                target.path(userId + RestShorts.FOLLOWERS)
                        .queryParam(RestShorts.PWD, password).request().accept(MediaType.APPLICATION_JSON).get(),
                String.class);
    }

    private Result<Void> clt_like(String shortId, String userId, boolean isLiked, String password) {
        // Update path based on the RestShorts interface definition for liking a short
        return super.toJavaResult(
                target.path(shortId + "/" + userId + RestShorts.LIKES)
                        .queryParam(RestShorts.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(isLiked, MediaType.APPLICATION_JSON))); // Update content type and value
    }

    private Result<List<String>> clt_likes(String shortId, String password) {
        // Update path based on the RestShorts interface definition for getting likes
        return super.toJavaResultForList(
                target.path(shortId + RestShorts.LIKES)
                        .queryParam(RestShorts.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(),
                String.class);
    }

    private Result<List<String>> clt_getFeed(String userId, String password) {
        // Update path based on the RestShorts interface definition for getting feed
        return super.toJavaResultForList(
                target.path(userId + RestShorts.FEED)
                        .queryParam(RestShorts.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(),
                String.class);
    }
}
