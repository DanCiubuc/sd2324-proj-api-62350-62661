package tukano.impl.grpc.clients;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;

public class GrpcBlobsClient implements Blobs {

    private static final long GRPC_REQUEST_TIMEOUT = 5000;
    final BlobsGrpc.BlobsBlockingStub stub;

    public GrpcBlobsClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = BlobsGrpc.newBlockingStub( channel ).withDeadlineAfter(GRPC_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        return toJavaResult(() -> {
            stub.upload(BlobsProtoBuf.UploadArgs.newBuilder()
                .setBlobId(blobId)
                .setData(ByteString.copyFrom(bytes))
                .build());
            return null;
        });
    }

    @Override
    public Result<byte[]> download(String blobId) {
        return toJavaResult(() -> {
            var res = stub.download(BlobsProtoBuf.DownloadArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return res.next().toByteArray();

        });
    }

    static <T> Result<T> toJavaResult(Supplier<T> func) {
        try {
            return ok(func.get());
        } catch(StatusRuntimeException sre) {
            var code = sre.getStatus().getCode();
            if( code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED )
                throw sre;
            return error( statusToErrorCode( sre.getStatus() ) );
        }
    }

    static Result.ErrorCode statusToErrorCode(Status status ) {
        return switch( status.getCode() ) {
            case OK -> Result.ErrorCode.OK;
            case NOT_FOUND -> Result.ErrorCode.NOT_FOUND;
            case ALREADY_EXISTS -> Result.ErrorCode.CONFLICT;
            case PERMISSION_DENIED -> Result.ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT -> Result.ErrorCode.BAD_REQUEST;
            case UNIMPLEMENTED -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
