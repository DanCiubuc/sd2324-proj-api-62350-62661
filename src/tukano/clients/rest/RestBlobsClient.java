package tukano.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;
import java.net.URI;

public class RestBlobsClient extends RestClient implements Blobs {
    final WebTarget target;

    public RestBlobsClient(URI serverURI) {
        super(serverURI);
        target = client.target(serverURI).path(RestBlobs.PATH);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        return super.reTry(() -> clt_upload(blobId, bytes));
    }

    @Override
    public Result<byte[]> download(String blobId) {
        return super.reTry(() -> clt_download(blobId));
    }

    public Result<Void> clt_upload(String blobId, byte[] bytes) {
        return super.toJavaResult(
                target.path(blobId)
                        .request()
                        .post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM)),
                null);
    }

    public Result<byte[]> clt_download(String blobId) {
        return super.toJavaResult(
                target.path(blobId)
                        .request()
                        .accept(MediaType.APPLICATION_OCTET_STREAM)
                        .get(),
                byte[].class);
    }

    @Override
    public Result<Void> remove(String blobId) {
        return super.toJavaResult(
                target.path(blobId)
                        .request().delete(),
                null);

    }

}
