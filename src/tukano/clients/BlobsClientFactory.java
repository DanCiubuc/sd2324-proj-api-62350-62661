package tukano.clients;

import java.net.URI;
import java.util.List;

import tukano.api.java.Blobs;
import tukano.clients.rest.RestBlobsClient;
import tukano.impl.grpc.clients.GrpcBlobsClient;
import utils.Discovery;

public class BlobsClientFactory {

    private static List<URI> blobsUris;
    private static int currentIdx = 0;

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
        return blobsUris.get(currentIdx);
    }

    public static List<URI> getBlobsUris() throws InterruptedException {
        Discovery disc = Discovery.getInstance();
        blobsUris = disc.knownUrisOf("blobs", 1);

        return blobsUris;
    }
}
