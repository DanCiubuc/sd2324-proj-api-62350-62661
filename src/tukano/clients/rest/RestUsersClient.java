package tukano.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
import tukano.api.rest.RestUsers;
import java.net.URI;
import java.util.List;


public class RestUsersClient extends RestClient implements Users {
    final WebTarget target;

    public RestUsersClient( URI serverURI ) {
        super(serverURI);

        target = client.target( serverURI ).path( RestUsers.PATH );
    }

    public Result<String> createUser(User user) {
        return super.reTry( () -> clt_createUser(user));
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        return super.reTry( () -> clt_getUser(userId, pwd));
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        return super.reTry( () -> clt_updateUser(userId, password, user));
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        return super.reTry( () -> clt_deleteUser(userId, password));
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return super.reTry( () -> clt_searchUsers(pattern));
    }

    private Result<String> clt_createUser(User user) {
        return super.toJavaResult(
                target.request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(user, MediaType.APPLICATION_JSON)), String.class );
    }

    private Result<User> clt_getUser(String userId, String pwd) {
        return super.toJavaResult(
                target.path( userId )
                        .queryParam(RestUsers.PWD, pwd).request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(), User.class);
    }

    private Result<User> clt_updateUser(String userId, String password, User user) {
        return super.toJavaResult(
                target.path(userId)
                        .queryParam(RestUsers.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .put(Entity.entity(user, MediaType.APPLICATION_JSON))
                        , User.class);
    }

    private Result<User> clt_deleteUser(String userId, String password) {
        return super.toJavaResult(
                target.path(userId)
                        .queryParam(RestUsers.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .delete(), User.class);
    }

    private Result<List<User>> clt_searchUsers(String pattern) {
        return super.toJavaResultForList(
                target.queryParam(RestUsers.QUERY, pattern)
                        .request()
                        .accept( MediaType.APPLICATION_JSON)
                        .get(), User.class);
    }
}
