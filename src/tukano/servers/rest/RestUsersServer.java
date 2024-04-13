package tukano.servers.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import utils.Discovery;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

public class RestUsersServer {
    private static Logger Log = Logger.getLogger(RestUsersServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 3456;
    public static final String SERVICE = "UsersService";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        try {

            ResourceConfig config = new ResourceConfig();
            config.register(RestUsersResource.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            Discovery disc = Discovery.getInstance();

            disc.announce(SERVICE, serverURI);

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
