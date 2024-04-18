package tukano.impl.grpc.servers;

import io.grpc.ServerBuilder;
import tukano.api.java.Blobs;
import utils.Discovery;

import java.net.InetAddress;
import java.util.logging.Logger;

public class GrpcBlobsServer {

    public static final int PORT = 15678;
    private static final String SERVICE = "blobs";
    private static final String SERVER_URI_FMT = "http://%s:%s/grpc";

    private static Logger Log = Logger.getLogger(GrpcBlobsServer.class.getName());

    public static void main(String[] args) throws Exception {

        var stub = new GrpcBlobsServerStub();
        var server = ServerBuilder.forPort(PORT).addService(stub).build();

        String ip = InetAddress.getLocalHost().getHostAddress();
        var serverURI = String.format(SERVER_URI_FMT, ip, PORT);

        Discovery disc = Discovery.getInstance();
        disc.announce(SERVICE, serverURI);

        Log.info(String.format("%s gRPC Server ready @ %s\n", SERVICE, serverURI));
        server.start().awaitTermination();
    }
}
