package tukano.clients;

import java.net.URI;

import tukano.api.java.Users;
import tukano.clients.rest.RestUsersClient;
import tukano.impl.grpc.clients.GrpcUsersClient;
import utils.Discovery;

public class UsersClientFactory {

    public static Users getClient() {
        Discovery disc = Discovery.getInstance();
        URI serverURI;
        try {
            serverURI = disc.knownUrisOf("users", 1).get(0);
            if (serverURI.toString().endsWith("rest"))
                return new RestUsersClient(serverURI);
            else
                return new GrpcUsersClient(serverURI);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }
}
