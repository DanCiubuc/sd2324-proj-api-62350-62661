package tukano.clients;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import tukano.api.java.Blobs;
import tukano.clients.rest.RestBlobsClient;
import tukano.impl.grpc.clients.GrpcBlobsClient;
import utils.Discovery;

public class BlobsClientFactory {
    private static Logger Log = Logger.getLogger(BlobsClientFactory.class.getName());

    private static List<URI> blobsUris;
    private static int currentIdx = 0;
    // Variable to insure a round-robin distribution. We need this to deal with
    // cases where the number of elements isn't perfectly divisible by the number of
    // blob servers
    private static int counter = 0;
    private static final String BLOB_URI_FORMAT = "%s/blobs";
    private static final String DELIMITER = "\t";

    public static synchronized Blobs getClient() throws InterruptedException {
        if (blobsUris == null) {
            blobsUris = getBlobsUris();
        }

        String blobUri = blobsUris.get(currentIdx).toString();
        currentIdx = getNextIdx(currentIdx);

        if (blobUri.endsWith("rest")) {
            return new RestBlobsClient(getUri());
        } else {
            return new GrpcBlobsClient(getUri());
        }
    }

    public static URI getUri() throws InterruptedException {
        if (blobsUris == null) {
            blobsUris = getBlobsUris();
        } else {
            currentIdx = getNextIdx(currentIdx);
        }

        return blobsUris.get(currentIdx);
    }

    public static Blobs getClient(String blobAddress) {
        URI blobUri = null;
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

    public static List<URI> getBlobsUris() throws InterruptedException {
        Discovery disc = Discovery.getInstance();
        blobsUris = disc.knownUrisOf("blobs", 1);
        return blobsUris;
    }

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
