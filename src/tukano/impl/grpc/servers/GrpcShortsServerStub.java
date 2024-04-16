package tukano.impl.grpc.servers;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.metamodel.Bindable;
import tukano.impl.grpc.generated_java.ShortsGrpc;
import tukano.impl.grpc.generated_java.ShortsGrpc.AsyncService;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.CreateShortArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.CreateShortResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.DeleteShortArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.DeleteShortResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.FollowArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.FollowResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.FollowersArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.FollowersResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetFeedArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetFeedResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortsArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortsResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.LikeArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.LikeResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.LikesArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.LikesResult;
import tukano.servers.java.JavaShorts;
import tukano.api.java.Result;
import tukano.api.java.Shorts;

public class GrpcShortsServerStub implements ShortsGrpc.AsyncService, BindableService {
    Shorts impl = new JavaShorts();

    @Override
    public ServerServiceDefinition bindService() {
        return ShortsGrpc.bindService(this);

    }

    @Override
    public void createShort(CreateShortArgs request, StreamObserver<CreateShortResult> responseObserver) {
        var res = impl.createShort(request.getUserId(), request.getPassword());
        if (!res.isOK())
            responseObserver.onError(errorCodeToStatus((res.error())));
        else {
            responseObserver.onNext(CreateShortResult.newBuilder().setValue(Short_to_GrpcShort(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteShort(DeleteShortArgs request, StreamObserver<DeleteShortResult> responseObserver) {
        // TODO Auto-generated method stub
        AsyncService.super.deleteShort(request, responseObserver);
    }

    @Override
    public void follow(FollowArgs request, StreamObserver<FollowResult> responseObserver) {
        // TODO Auto-generated method stub
        AsyncService.super.follow(request, responseObserver);
    }

    @Override
    public void followers(FollowersArgs request, StreamObserver<FollowersResult> responseObserver) {
        // TODO Auto-generated method stub
        AsyncService.super.followers(request, responseObserver);
    }

    @Override
    public void getFeed(GetFeedArgs request, StreamObserver<GetFeedResult> responseObserver) {
        // TODO Auto-generated method stub
        AsyncService.super.getFeed(request, responseObserver);
    }

    @Override
    public void getShort(GetShortArgs request, StreamObserver<GetShortResult> responseObserver) {
        // TODO Auto-generated method stub
        AsyncService.super.getShort(request, responseObserver);
    }

    @Override
    public void getShorts(GetShortsArgs request, StreamObserver<GetShortsResult> responseObserver) {
        // TODO Auto-generated method stub
        AsyncService.super.getShorts(request, responseObserver);
    }

    @Override
    public void like(LikeArgs request, StreamObserver<LikeResult> responseObserver) {
        // TODO Auto-generated method stub
        AsyncService.super.like(request, responseObserver);
    }

    @Override
    public void likes(LikesArgs request, StreamObserver<LikesResult> responseObserver) {
        // TODO Auto-generated method stub
        AsyncService.super.likes(request, responseObserver);
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
