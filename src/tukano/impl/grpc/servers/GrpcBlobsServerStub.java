package tukano.impl.grpc.servers;
import com.google.protobuf.ByteString;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.servers.java.JavaBlobs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadResult;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadResult;
import utils.Hash;
import utils.Hex;

import java.util.logging.Logger;

public class GrpcBlobsServerStub implements BlobsGrpc.AsyncService, BindableService {
    Blobs impl = new JavaBlobs();

    private static Logger Log = Logger.getLogger(GrpcBlobsServerStub.class.getName());


    @Override
    public ServerServiceDefinition bindService() {
        return BlobsGrpc.bindService(this);
    }

    @Override
    public void upload(UploadArgs request, StreamObserver<UploadResult> responseObserver) {
        var res = impl.upload(request.getBlobId(), request.getData().toByteArray());
        if (!res.isOK())
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(UploadResult.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void download(DownloadArgs request, StreamObserver<DownloadResult> responseObserver) {
        var res = impl.download(request.getBlobId());
        if (!res.isOK())
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            Log.info(() -> String.format("downloadStub : sha256 = %s\n", Hex.of(Hash.sha256(res.value()))));
            Log.info(() -> String.format("Converted downloadStub : sha256 = %s\n", Hex.of(Hash.sha256(ByteString.copyFrom(res.value()).toByteArray()))));
            responseObserver.onNext(DownloadResult.newBuilder().setChunk(ByteString.copyFrom(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    protected static Throwable errorCodeToStatus(Result.ErrorCode error) {
        var status = switch (error) {
            case NOT_FOUND -> io.grpc.Status.NOT_FOUND;
            case CONFLICT -> io.grpc.Status.ALREADY_EXISTS;
            case FORBIDDEN -> io.grpc.Status.PERMISSION_DENIED;
            case NOT_IMPLEMENTED -> io.grpc.Status.UNIMPLEMENTED;
            case BAD_REQUEST -> io.grpc.Status.INVALID_ARGUMENT;
            default -> io.grpc.Status.INTERNAL;
        };

        return status.asException();
    }
}
