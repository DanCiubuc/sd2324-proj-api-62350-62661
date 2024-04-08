package tukano.impl.grpc.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;



public class GetUser{
    private static Logger Log = Logger.getLogger(GetUser.class.getName());

    public static void main(String[] args) throws IOException {
        if( args.length != 3) {
            System.err.println( "Use: java lab2.clients.GetUser url userId password");
            return;
        }

        String serverUrl = args[0];
        String userId = args[1];
        String pwd = args[2];

        var client = new GrpcUsersClient( URI.create( serverUrl ) );

        var result = client.getUser(userId, pwd);
        if( result.isOK()  )
            Log.info("Get user:" + result.value() );
        else
            Log.info("Get user failed with error: " + result.error());
    }

}