package tukano.servers.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import utils.Discovery;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

public class RestShortsServer {
    private static Logger Log = Logger.getLogger(RestShortsServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 4567;
    public static final String SERVICE = "shorts";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        try {

            ResourceConfig config = new ResourceConfig();
            config.register(RestShortsResouce.class);

            String serverURI = String.format(SERVER_URI_FMT, SERVICE, PORT);
            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            Discovery disc = Discovery.getInstance();

            disc.announce(SERVICE, serverURI);

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
