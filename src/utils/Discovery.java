package utils;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * <p>
 * A class interface to perform service discovery based on periodic
 * announcements over multicast communication.
 * </p>
 * 
 */

public interface Discovery {

    /**
     * Used to announce the URI of the given service name.
     * 
     * @param serviceName - the name of the service
     * @param serviceURI  - the uri of the service
     */
    public void announce(String serviceName, String serviceURI);

    /**
     * Get discovered URIs for a given service name
     * 
     * @param serviceName - name of the service
     * @param minReplies  - minimum number of requested URIs. Blocks until the
     *                    number is satisfied.
     * @return array with the discovered URIs for the given service name.
     * @throws InterruptedException
     */
    public List<URI> knownUrisOf(String serviceName, int minReplies) throws InterruptedException;

    /**
     * Get the instance of the Discovery service
     * 
     * @return the singleton instance of the Discovery service
     */
    public static Discovery getInstance() {
        return DiscoveryImpl.getInstance();
    }
}

/**
 * Implementation of the multicast discovery service
 */
class DiscoveryImpl implements Discovery {

    private static Logger Log = Logger.getLogger(Discovery.class.getName());

    // TODO: utilizar shorts.props file

    // The pre-aggreed multicast endpoint assigned to perform discovery.

    static final int DISCOVERY_RETRY_TIMEOUT = 5000;
    static final int DISCOVERY_ANNOUNCE_PERIOD = 1000;

    // Replace with appropriate values: allowed IP Multicast range: 224.0.0.1 -
    // 239.255.255.255
    static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2262);

    // Used to separate the two fields that make up a service announcement.
    private static final String DELIMITER = "\t";

    private static final int MAX_DATAGRAM_SIZE = 65536;

    private static Discovery singleton;
    private boolean clientGotUri = false;

    synchronized static Discovery getInstance() {
        if (singleton == null) {
            singleton = new DiscoveryImpl();
        }
        return singleton;
    }

    // private Map<String, URI> mapping = new HashMap<>();

    private DiscoveryImpl() {
        this.startListener();
    }

    @Override
    public void announce(String serviceName, String serviceURI) {
        Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s\n", DISCOVERY_ADDR, serviceName,
                serviceURI));

        var pktBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();
        var pkt = new DatagramPacket(pktBytes, pktBytes.length, DISCOVERY_ADDR);

        // start thread to send periodic announcements
        new Thread(() -> {
            try (var ds = new DatagramSocket()) {
                while (true) {
                    try {
                        ds.send(pkt);
                        Thread.sleep(DISCOVERY_ANNOUNCE_PERIOD);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private final Map<String, List<URI>> mapping = new ConcurrentHashMap<>(); // Thread-safe map

    @Override
    public synchronized List<URI> knownUrisOf(String serviceName, int minEntries) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (mapping.get(serviceName) == null || mapping.size() < minEntries) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > DISCOVERY_RETRY_TIMEOUT) {
                // Timeout reached, return null or empty array
                return null;
            }
            wait(DISCOVERY_RETRY_TIMEOUT - elapsedTime); // Wait for remaining timeout
        }

        // System.out.println(mapping);
        // flag to signal to the listener thread that this instance of discovery no
        // longer is necessary, since the client already got the service he wanted
        clientGotUri = true;
        // Return all URIs for the service name (modify if needed)
        return mapping.get(serviceName);
    }

    private void startListener() {
        Log.info(String.format("Starting discovery on multicast group: %s, port: %d\n", DISCOVERY_ADDR.getAddress(),
                DISCOVERY_ADDR.getPort()));

        new Thread(() -> {
            try (var ms = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
                ms.joinGroup(DISCOVERY_ADDR, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
                for (;;) {
                    try {
                        if (clientGotUri) {
                            break;
                        }
                        var pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);
                        ms.receive(pkt);

                        var msg = new String(pkt.getData(), 0, pkt.getLength());
                        // Log.info(String.format("Received: %s", msg));

                        var parts = msg.split(DELIMITER);
                        if (parts.length == 2) {
                            var serviceName = parts[0];
                            var uri = URI.create(parts[1]);

                            if (mapping.get(serviceName) == null) {
                                mapping.put(serviceName, new ArrayList<>());
                            }

                            List<URI> serviceUris = mapping.get(serviceName);
                            serviceUris.add(uri);
                            mapping.put(serviceName, serviceUris);
                        }

                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }).start();
    }
}
