# base ubuntu official image 
FROM ubuntu

# run a command (install a package)
RUN apt-get update && apt-get install iproute2 -y

# Copy openjdk 17 from another image
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:17 $JAVA_HOME $JAVA_HOME
ENV PATH=$PATH:$JAVA_HOME/bin

# working directory inside docker image
WORKDIR /home/sd

# copy the jar created by assembly to the docker image
COPY target/*jar-with-dependencies.jar sd.jar

# cd IdeaProjects/sd2324-proj-api-62350-62661

# mvn clean compile assembly:single docker:build

############################################################# REST ################################################################################

# Start Server
# docker run --rm -h users-1 --name users-1 --network sdnet -p 8080:8080 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.servers.rest.RestUsersServer

# Start Client
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy /bin/bash

# Create User
# java -cp sd.jar tukano.clients.rest.RestClientClass create UsersService nmp 12345 nmp@nova.pt "Nuno Preguica"

# Get User
# java -cp sd.jar tukano.clients.rest.RestClientClass get UsersService nmp 12345

# Update User
# java -cp sd.jar tukano.clients.rest.RestClientClass update UsersService nmp 12345 hahaha@nova.pt "João Bombaclaat"

# Delete User
# java -cp sd.jar tukano.clients.rest.RestClientClass delete UsersService nmp 12345

# Search Users
# java -cp sd.jar tukano.clients.rest.RestClientClass search UsersService nmp

# Upload Blobs
# java -cp sd.jar tukano.clients.rest.RestBlobsClientClass upload http://80:80/rest nmp fileName

############################################################# GRPC ################################################################################

# Start Server
# docker run --rm -h users-1 --network sdnet -p 9000:9000 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.servers.GrpcUsersServer

# Create User
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.clients.GrpcClientClass create grpc://users-1:9000/gprc nmp 12345 nmp@nova.unl.pt "Nuno Preguica"

# Get User
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.clients.GrpcClientClass get grpc://users-1:9000/gprc nmp 12345

# Update User
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.clients.GrpcClientClass update grpc://users-1:9000/gprc nmp 12345 hahaha@nova.pt "João Bombaclaat"

# Delete User
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.clients.GrpcClientClass delete grpc://users-1:9000/gprc nmp 12345

# Search Users
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.clients.GrpcClientClass searcg grpc://users-1:9000/gprc nmp