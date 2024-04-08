package tukano.impl.grpc.common;

import tukano.api.User;
import tukano.impl.grpc.generated_java.UsersProtoBuf.GrpcUser;

public class DataModelAdaptor {

	public static User GrpcUser_to_User( GrpcUser from )  {
		return new User( 
				from.getUserId(), 
				from.getPassword(), 
				from.getEmail(), 
				from.getDisplayName());
	}

	public static GrpcUser User_to_GrpcUser( User from )  {
		return GrpcUser.newBuilder()
				.setUserId( from.getUserId())
				.setPassword( from.getPwd())
				.setEmail( from.getEmail())
				.setDisplayName( from.getDisplayName())
				.build();
	}

}
