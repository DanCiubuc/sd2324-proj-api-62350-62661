package tukano.impl.grpc.servers;

import java.net.InetAddress;
import java.util.logging.Logger;

import io.grpc.ServerBuilder;
import utils.Discovery;

public class GrpcShortsServer {
    public static final int PORT = 14567;

    public static final String SERVICE = "shorts";
    private static final String SERVER_URI_FMT = "http://%s:%s/grpc";

    private static Logger Log = Logger.getLogger(GrpcShortsServer.class.getName());

    public static void main(String[] args) throws Exception {

        var stub = new GrpcShortsServerStub();
        var server = ServerBuilder.forPort(PORT).addService(stub).build();

        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverURI = String.format(SERVER_URI_FMT, ip, PORT);

        Discovery disc = Discovery.getInstance();

        disc.announce(SERVICE, serverURI);

        Log.info(String.format("%s gRPC Server ready @ %s\n", SERVICE, serverURI));
        server.start().awaitTermination();
    }

}
