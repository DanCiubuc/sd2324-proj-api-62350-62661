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

import static tukano.impl.grpc.common.DataModelAdaptor.User_to_GrpcUser;

public class GrpcBlobsServerStub implements BlobsGrpc.AsyncService, BindableService {
    Blobs impl = new JavaBlobs();

    @Override
    public ServerServiceDefinition bindService() {
        return BlobsGrpc.bindService(this);
    }

    @Override
    public void upload(UploadArgs request, StreamObserver<UploadResult> responseObserver) {
        var res = impl.upload(request.getBlobId(), request.getBlobIdBytes().toByteArray());
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
            byte[] bytes = res.value();
            ByteString b = ByteString.copyFrom(bytes);
            responseObserver.onNext(DownloadResult.newBuilder().setChunk(ByteString.copyFrom(bytes)).build());
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
