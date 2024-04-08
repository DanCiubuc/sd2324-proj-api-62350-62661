package tukano.impl.grpc.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import tukano.api.User;

public class UpdateUser {

    private static Logger Log = Logger.getLogger(UpdateUser.class.getName());

    public static void main(String[] args) throws IOException {

        if (args.length != 5) {
            System.err.println("Use: java lab3.clients.UpdateUser url userId password ");
            return;
        }

        String serverUrl = args[0];
        String userId = args[1];
        String password = args[2];
        String email = args[3];
        String displayName = args[4];

        var client = new GrpcUsersClient(URI.create(serverUrl));
        var user = new User(userId, password, email, displayName);

        var result = client.updateUser(userId, password, user);
        if (result.isOK())
            Log.info("Updated user:" + result.value());
        else
            Log.info("Update user failed with error: " + result.error());
    }
}
