package tukano.clients.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class RestBlobsClientClass {

    private static Logger Log = Logger.getLogger(RestBlobsClientClass.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        String command = args[0];
        String url = args[1];
        var client = new RestBlobsClient(URI.create( url ));

        switch (command) {
            case "upload":
                upload(args, client);
                break;
            case "download":
                download(args, client);
                break;
        }
    }

    private static void upload(String[] args, RestBlobsClient client) throws IOException {
        if (args.length != 4) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestClientClass upload url blobId fileName ");
            return;
        }
        String blobId = args[2];
        String fileName = args[3];

        try (FileInputStream fis = new FileInputStream(fileName)) {
            byte[] bytes = new byte[fis.available()]; // Create a byte array to store the read bytes
            int bytesRead  = fis.read(bytes); // Read bytes from the file

            Log.info("Bytes read from file: " + bytesRead);

            var result = client.upload(blobId, bytes);

            if (result.isOK())
                Log.info("Uploaded file:" + result.value());
            else
                Log.info("Upload file failed with error: " + result.error());

        } catch (IOException e) {
            e.printStackTrace(); // Handle file reading errors
        }
    }

    private static void download(String[] args, RestBlobsClient client) throws IOException {
        if (args.length != 3) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestClientClass download url blobId ");
            return;
        }
        String blobId = args[2];

        var result = client.download(blobId);

        if (result.isOK())
            Log.info("Downloaded file:" + result.value());
        else
            Log.info("Download file failed with error: " + result.error());

    }
}
