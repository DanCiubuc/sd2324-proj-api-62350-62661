package tukano.clients;

import tukano.api.java.Blobs;
import tukano.clients.rest.RestBlobsClient;
import tukano.impl.grpc.clients.GrpcBlobsClient;
import utils.Discovery;

public class BlobsClientFactory {

    public static Blobs getClient() throws InterruptedException {
        Discovery disc = Discovery.getInstance();
        var serverURI = disc.knownUrisOf("BlobsService", 1);
        if (serverURI.toString().endsWith("rest"))
            return new RestBlobsClient(serverURI);
        else
            return new GrpcBlobsClient(serverURI);
    }
}
