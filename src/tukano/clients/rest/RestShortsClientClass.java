package tukano.clients.rest;

import utils.Discovery;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class RestShortsClientClass {
    private static Logger Log = Logger.getLogger(RestShortsClientClass.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        String command = args[0];
        String serviceName = args[1];
        // Nota: aqui era preciso adicionar um check ou refatorizar as classes todas dos
        // clientes, mas visto que isto não sera entregue, não vale a pena estar a ter
        // mais trabalho
        Discovery disc = Discovery.getInstance();
        URI serverUrl = disc.knownUrisOf(serviceName, 1);
        var client = new RestShortsClient(serverUrl);

        switch (command) {
            case "create":
                createShort(args, client);
                break;
            case "get":
                getShort(args, client);
                break;

        }
    }

    private static void getShort(String[] args, RestShortsClient client) {
        if (args.length != 3) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestShortsClientClass get shorts shortId");
            return;
        }
        String shortId = args[2];

        var result = client.getShort(shortId);

        if (result.isOK())
            Log.info("Get short:" + result.value());
        else
            Log.info("Get short failed with error: " + result.error());
    }

    private static void createShort(String[] args, RestShortsClient client) {
        if (args.length != 4) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestShortsClientClass create shorts userId password");
            return;
        }
        String userId = args[2];
        String pwd = args[3];

        var result = client.createShort(userId, pwd);

        if (result.isOK())
            Log.info("Created short:" + result.value());
        else
            Log.info("Create short failed with error: " + result.error());
    }

}
