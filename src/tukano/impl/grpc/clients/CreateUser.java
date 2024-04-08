package tukano.impl.grpc.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import tukano.api.User;

public class CreateUser {
	private static Logger Log = Logger.getLogger(CreateUser.class.getName());

	public static void main(String[] args) throws IOException {
		
		if( args.length != 5) {
			System.err.println( "Use: java lab2.clients.CreateUser url userId password email displayName ");
			return;
		}
		
		String serverUrl = args[0];
		String userId = args[1];
		String password = args[2];
		String email = args[3];
		String displayName = args[4];
		
		var client = new GrpcUsersClient( URI.create( serverUrl ) );
		
		var user = new User( userId, password, email, displayName);		
		
		var result = client.createUser( user );
		if( result.isOK()  )
			Log.info("Created user:" + result.value() );
		else
			Log.info("Create user failed with error: " + result.error());
	}
	
}
