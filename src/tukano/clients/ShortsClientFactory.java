package tukano.clients;

import java.net.URI;

import tukano.api.java.Shorts;
import tukano.clients.rest.RestShortsClient;
import tukano.impl.grpc.clients.GrpcShortsClient;
import utils.Discovery;

public class ShortsClientFactory {

    private static URI shortsUris;

    public static Shorts getClient() {
        if (shortsUris == null) {
            shortsUris = getShortsUris();
        }
        if (shortsUris.toString().endsWith("rest"))
            return new RestShortsClient(shortsUris);
        else
            return new GrpcShortsClient(shortsUris);
    }

    public static URI getShortsUris() {
        try {
            Discovery disc = Discovery.getInstance();
            shortsUris = disc.knownUrisOf("shorts", 1).get(0);
        } catch (Exception e) {
            System.err.println(e);
        }
        return shortsUris;

    }

}
