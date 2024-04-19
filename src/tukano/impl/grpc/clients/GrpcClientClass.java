package tukano.impl.grpc.clients;

import tukano.api.User;
import tukano.clients.rest.RestUsersClient;
import utils.Discovery;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class GrpcClientClass {

    private static Logger Log = Logger.getLogger(GrpcClientClass.class.getName());
    public static void main(String[] args) throws IOException, InterruptedException {
        String command = args[0];
        String serviceName = args[1];
        Discovery disc = Discovery.getInstance();
        URI serverUrl = disc.knownUrisOf(serviceName, 1).get(0);
        var client = new GrpcUsersClient(serverUrl);

        switch (command) {
            case "create":
                createUser(args, client);
                break;
            case "get":
                getUser(args, client);
                break;
            case "update":
                updateUser(args, client);
                break;
            case "delete":
                deleteUser(args, client);
                break;
            case "search":
                searchUsers(args, client);
                break;
        }
    }

    private static void createUser(String[] args, GrpcUsersClient client) throws IOException {
        if( args.length != 6) {
            System.err.println( "Use: java tukano.clients.rest.RestClientClass create url userId password email displayName ");
            return;
        }

        String userId = args[2];
        String password = args[3];
        String email = args[4];
        String displayName = args[5];

        var user = new User( userId, password, email, displayName);

        var result = client.createUser( user );

        if( result.isOK()  )
            Log.info("Created user:" + result.value() );
        else
            Log.info("Create user failed with error: " + result.error());
    }

    private static void getUser(String[] args, GrpcUsersClient client) throws IOException {
        if( args.length != 4) {
            System.err.println( "Use: java tukano.clients.rest.RestClientClass get url userId password");
            return;
        }

        String userId = args[2];
        String pwd = args[3];

        var result = client.getUser(userId, pwd);
        if( result.isOK()  )
            Log.info("Get user:" + result.value() );
        else
            Log.info("Get user failed with error: " + result.error());
    }

    private static void updateUser(String[] args, GrpcUsersClient client) throws IOException {
        if( args.length != 6) {
            System.err.println( "Use: java tukano.clients.rest.RestClientClass update url userId password email displayName");
            return;
        }

        String userId = args[2];
        String password = args[3];
        String email = args[4];
        String displayName = args[5];

        var user = new User( userId, password, email, displayName);

        var result = client.updateUser(userId, password, user);
        if( result.isOK()  )
            Log.info("Updated user:" + result.value() );
        else
            Log.info("Update user failed with error: " + result.error());
    }

    private static void deleteUser(String[] args, GrpcUsersClient client) throws IOException {
        if( args.length != 4) {
            System.err.println( "Use: java tukano.clients.rest.RestClientClass delete url userId password ");
            return;
        }

        String userId = args[2];
        String password = args[3];


        var result = client.deleteUser(userId, password);
        if( result.isOK()  )
            Log.info("Deleted user:" + result.value() );
        else
            Log.info("Delete user failed with error: " + result.error());
    }

    private static void searchUsers(String[] args, GrpcUsersClient client) throws IOException {
        if (args.length != 3) {
            System.err.println( "Use: java tukano.clients.rest.RestClientClass search url pattern ");
            return;
        }

        String pattern = args[2];

        var result = client.searchUsers(pattern);
        if(result.isOK())
            Log.info("Searched users:" + result.value() );
        else
            Log.info("Search users failed with error: " + result.error());
    }
}
