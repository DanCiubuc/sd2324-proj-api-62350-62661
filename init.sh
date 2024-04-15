#!/bin/bash

# Function to kill all child processes (terminals)
kill_all_terminals() {
  echo "Killing all child processes..."
  pkill -P $$  # Kill all child processes of the current process
  exit 1
}

# Trap termination signals to kill child processes
trap kill_all_terminals SIGTERM SIGINT

# Set Docker context to default
docker context use default

# Define docker commands for each server
user_server_cmd="docker run --rm -h users --name users --network sdnet -p 3456:3456 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.servers.rest.RestUsersServer"
shorts_server_cmd="docker run --rm -h shorts --name shorts --network sdnet -p 4567:4567 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.servers.rest.RestShortsServer"
blobs_server_cmd="docker run --rm -h blobs1 --name blobs1 --network sdnet -p 5678:5678 sd2324-tp1-api-xxxxx-yyyyy java -cp sd.jar tukano.servers.rest.RestBlobsServer 1"
client_cmd="docker run -it --network sdnet sd2324-tp1-api-xxxxx-yyyyy /bin/bash"

# Start servers in separate terminals
gnome-terminal -- bash -c "$user_server_cmd" &
gnome-terminal -- bash -c "$shorts_server_cmd" &
gnome-terminal -- bash -c "$blobs_server_cmd" &

# Start client in a separate terminal (interactive)
gnome-terminal -- $client_cmd



