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
    private static final String BLOB_URI_FORMAT = "%s/blobs";
    private static final String DELIMITER = "\t";

    public static synchronized Blobs getClient() throws InterruptedException {
        if (blobsUris == null) {
            blobsUris = getBlobsUris();
        }

        // Circular selection of available blobs servers
        int idx = currentIdx % blobsUris.size();
        currentIdx = (currentIdx + 1) % blobsUris.size();

        if (blobsUris.get(idx).toString().endsWith("rest")) {
            return new RestBlobsClient(getUri());
        } else {
            return new GrpcBlobsClient(getUri());
        }
    }

    public static URI getUri() throws InterruptedException {
        if (blobsUris == null) {
            blobsUris = getBlobsUris();
        }

        currentIdx = (currentIdx + 1) % blobsUris.size();
        return blobsUris.get(currentIdx);
    }

    public static Blobs getClient(String blobAddress) {
        URI blobUri = null;
        for (URI uri : blobsUris) {
            Log.info(uri.toString().split(DELIMITER)[0]);
            if (uri.toString().split(DELIMITER)[0].equals(blobAddress))
                blobUri = uri;
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
}
