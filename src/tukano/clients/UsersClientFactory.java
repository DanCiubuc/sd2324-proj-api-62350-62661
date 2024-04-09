package tukano.clients;

import tukano.api.java.Users;
import tukano.clients.rest.RestUsersClient;
import tukano.impl.grpc.clients.GrpcUsersClient;
import utils.Discovery;

public class UsersClientFactory {

    public static Users getClient() throws InterruptedException {
        Discovery disc = Discovery.getInstance();
        var serverURI = disc.knownUrisOf("UsersService", 1);
        if (serverURI.toString().endsWith("rest"))
            return new RestUsersClient(serverURI);
        else
            return new GrpcUsersClient(serverURI);
    }
}
