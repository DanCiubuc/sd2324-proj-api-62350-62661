package tukano.impl.grpc.servers;

import java.net.InetAddress;
import java.util.logging.Logger;

import io.grpc.ServerBuilder;
import tukano.api.java.Users;

public class GrpcUsersServer {
public static final int PORT = 9000;

	private static final String GRPC_CTX = "/gprc";
	private static final String SERVER_BASE_URI = "grpc://%s:%s%s";
	
	private static Logger Log = Logger.getLogger(GrpcUsersServer.class.getName());
	
	public static void main(String[] args) throws Exception {
		
		var stub = new GrpcUsersServerStub();
		var server = ServerBuilder.forPort(PORT).addService(stub).build();
		var serverURI = String.format(SERVER_BASE_URI, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);

		Log.info(String.format("%s gRPC Server ready @ %s\n", Users.NAME, serverURI));
		server.start().awaitTermination();
	}
}
