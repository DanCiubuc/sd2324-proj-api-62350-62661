FROM smduarte/sd2324testerbase

# working directory inside docker image
WORKDIR /home/sd

ADD hibernate.cfg.xml .
ADD shorts.props .

# copy the jar created by assembly to the docker image
COPY target/*jar-with-dependencies.jar sd2324.jar

# source .bash_profile

# cd IdeaProjects/sd2324-proj-api-62350-62661

# mvn clean compile assembly:single docker:build

############################################################# TESTER ################################################################################
# sh test-sd-tp1.sh -image sd2324-tp1-api-xxxxx-yyyyy
# sh test-sd-tp1.sh -image sd2324-tp1-api-xxxxx-yyyyy -test num -log ALL


############################################################# REST ################################################################################

# Start Server
# docker run --rm -h users --name users --network sdnet -p 3456:3456 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.servers.rest.RestUsersServer

# Start Shorts Server
# docker run --rm -h shorts --name shorts --network sdnet -p 4567:4567 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.servers.rest.RestShortsServer

# Start Blobs Server
# docker run --rm -h blobs1 --name blobs1 --network sdnet -p 5678:5678 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.servers.rest.RestBlobsServer 1

# Start Client
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy /bin/bash

# OR EXECUTE SCRIPT WITH COMMAND: ./init.sh

# --------------------------------------------------- Test Blob Server ----------------------------------------------------------------------------------#

# Create User
# java -cp sd.jar tukano.clients.rest.RestClientClass create users nmp 12345 nmp@nova.pt "Nuno Preguica"

# java -cp sd.jar tukano.clients.rest.RestClientClass create users jb 12345 jb@nova.pt "Joao Bombaclat"

# Create Shorts
# java -cp sd.jar tukano.clients.rest.RestShortsClientClass create shorts nmp 12345

# Get Short
# java -cp sd.jar tukano.clients.rest.RestShortsClientClass getShort shorts (shortId)

# Get Shorts
# java -cp sd.jar tukano.clients.rest.RestShortsClientClass getShorts shorts nmp

# Create text file in client
# echo hello from the client > hello.txt

# Substitute the first argument with the url of the blob server, and the second one with the blobId created during the Create Short command

# Delete Short
# java -cp sd.jar tukano.clients.rest.RestShortsClientClass delete shorts shortId 12345

# Follow
# java -cp sd.jar tukano.clients.rest.RestShortsClientClass follow shorts nmp jb true 12345

# Followers
# java -cp sd.jar tukano.clients.rest.RestShortsClientClass followers shorts nmp 12345

# Like
# java -cp sd.jar tukano.clients.rest.RestShortsClientClass like shorts 3914462230858879 nmp true 12345

# Likes
# java -cp sd.jar tukano.clients.rest.RestShortsClientClass likes shorts 3914462230858879 12345

# Get Feed
# java -cp sd.jar tukano.clients.rest.RestShortsClientClass getFeed shorts jb 12345

# Upload to blob
# java -cp sd.jar tukano.clients.rest.RestBlobsClientClass upload http://blobs1:5678/rest/ 6230367752181219 hello.txt

# Download blob
# java -cp sd.jar tukano.clients.rest.RestBlobsClientClass download http://blobs1:5678/rest/ 6230367752181219

# ----------------------------------------------------------------------------------------------------------------------------------------------------#

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

# Start Users Server
# docker run --rm -h users-1 --network sdnet -p 9000:9000 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.servers.GrpcUsersServer

# Start Shorts Server
# docker run --rm -h shorts --name shorts --network sdnet -p 4567:4567 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.servers.GrpcShortsServer

# Start Blobs Server
# docker run --rm -h blobs1 --name blobs1 --network sdnet -p 5678:5678 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.servers.GrpcBlobsServer 1


# Create User
# java -cp sd.jar tukano.impl.grpc.clients.GrpcClientClass create users nmp 12345 nmp@nova.unl.pt "Nuno Preguica"

# Get User
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.clients.GrpcClientClass get grpc://users-1:9000/gprc nmp 12345

# Update User
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.clients.GrpcClientClass update grpc://users-1:9000/gprc nmp 12345 hahaha@nova.pt "João Bombaclaat"

# Delete User
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.clients.GrpcClientClass delete grpc://users-1:9000/gprc nmp 12345

# Search Users
# docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.impl.grpc.clients.GrpcClientClass searcg grpc://users-1:9000/gprc nmp