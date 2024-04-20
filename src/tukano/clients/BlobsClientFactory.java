package tukano.clients;

import java.net.URI;
import java.util.List;

import tukano.api.java.Blobs;
import tukano.clients.rest.RestBlobsClient;
import tukano.impl.grpc.clients.GrpcBlobsClient;
import utils.Discovery;

/**
 * Factory method to retrieve a client for the blobs service. Used by the
 * business logic side of other services. Ensures that services work with each
 * other, independently of being REST or GRPC.
 * 
 */
public class BlobsClientFactory {
    private static List<URI> blobsUris;
    private static int currentIdx = 0;
    // Variable to insure a round-robin distribution. We need this to deal with
    // cases where the number of elements isn't perfectly divisible by the number of
    // blob servers
    private static int counter = 0;
    private static final String DELIMITER = "\t";
    private static final String SERVICE_NAME = "blobs";
    private static final int DISCOVERY_MIN_REPLIES = 1;

    /**
     * Retrieves a client for one of the available blobs service. The selection is
     * made in a round-robin fashion, in order to ensure a balanced distribution. If
     * you wish to retrieve a specific client, provide the blob URI as an argument.
     * 
     * @return Blobs object
     * @throws InterruptedException
     */
    public static synchronized Blobs getClient() throws InterruptedException {
        if (blobsUris == null) {
            blobsUris = getBlobsUris();
        }

        String blobUri = blobsUris.get(currentIdx).toString();
        // advance to next instance of service
        currentIdx = getNextIdx(currentIdx);

        if (blobUri.endsWith("rest")) {
            return new RestBlobsClient(getUri());
        } else {
            return new GrpcBlobsClient(getUri());
        }
    }

    /**
     * Retrieves the client associated with a specific blob address
     * 
     * @param blobAddress
     * @return Blobs object, if the URI was found
     *         null, if there is no server found with the specified URI
     */
    public static Blobs getClient(String blobAddress) {
        URI blobUri = null;

        // find the URI first in the list of URIs
        for (int i = 0; i < blobsUris.size(); i++) {
            URI uri = blobsUris.get(i);
            if (uri.toString().split(DELIMITER)[0].equals(blobAddress)) {
                blobUri = uri;
            }
        }
        if (blobUri == null) {
            return null;
        }
        if (blobUri.toString().endsWith("rest")) {
            return new RestBlobsClient(blobUri);
        } else {
            return new GrpcBlobsClient(blobUri);
        }

    }

    /**
     * Retrieves a URI for one of the available blob servers. Used by createShort,
     * in order to generate a blob resource url
     * 
     * @return URI of one the available blob server
     * @throws InterruptedException
     */
    public static URI getUri() throws InterruptedException {
        // i.e. if it is the first time the Factory was used by a service
        if (blobsUris == null) {
            blobsUris = getBlobsUris();
        } else {
            currentIdx = getNextIdx(currentIdx);
        }

        return blobsUris.get(currentIdx);
    }

    /**
     * Helper method to get the Blob URIs, using the Discovery service.
     * 
     * @return List<URI> URIs of the available blob servers
     * @throws InterruptedException
     */
    private static List<URI> getBlobsUris() throws InterruptedException {
        // Discovery is a singleton class
        Discovery disc = Discovery.getInstance();
        blobsUris = disc.knownUrisOf(SERVICE_NAME, DISCOVERY_MIN_REPLIES);
        return blobsUris;
    }

    /**
     * Helper method to compute the index of the next URI to select. This is done in
     * order to ensure the balance of loads between servers.
     * 
     * @param idx current index
     * @return next index
     */
    private static int getNextIdx(int idx) {
        int result = (idx + counter) % blobsUris.size();
        counter++;
        // once every blob has recieved an element, reset the counter
        if (counter == blobsUris.size()) {
            counter = 0;
        }

        return result;
    }
}
