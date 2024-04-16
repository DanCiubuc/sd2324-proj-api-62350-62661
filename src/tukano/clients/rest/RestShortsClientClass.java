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
        URI serverUrl = disc.knownUrisOf(serviceName, 1).get(0);
        var client = new RestShortsClient(serverUrl);

        switch (command) {
            case "create":
                createShort(args, client);
                break;
            case "getShort":
                getShort(args, client);
                break;
            case "getShorts":
                getShorts(args, client);
                break;
            case "delete":
                deleteShort(args, client);
                break;
            case "follow":
                follow(args, client);
                break;
            case "followers":
                followers(args, client);
                break;
            case "like":
                like(args, client);
                break;
            case "likes":
                likes(args, client);
                break;
            case "getFeed":
                getFeed(args, client);
                break;
            default:
                System.err.println("Invalid command: " + command);
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

    private static void getShorts(String[] args, RestShortsClient client) {
        if (args.length != 3) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestShortsClientClass getShorts shorts userId");
            return;
        }
        String userId = args[2];

        var result = client.getShorts(userId);

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

    private static void deleteShort(String[] args, RestShortsClient client) {
        if (args.length != 4) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestShortsClientClass delete shorts shortId password");
            return;
        }
        String shortId = args[2];
        String pwd = args[3];

        var result = client.deleteShort(shortId, pwd);

        if (result.isOK())
            Log.info("Deleted short:" + shortId);
        else
            Log.info("Delete short failed with error: " + result.error());
    }

    private static void follow(String[] args, RestShortsClient client) {
        if (args.length != 6) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestShortsClientClass follow shorts userId1 userId2 follow (true/false) password");
            return;
        }
        String userId1 = args[2];
        String userId2 = args[3];
        boolean isFollowing = Boolean.parseBoolean(args[4]);
        String pwd = args[5];

        var result = client.follow(userId1, userId2, isFollowing, pwd);

        if (result.isOK())
            Log.info("Followed user: " + userId2 + (isFollowing ? "" : " (unfollowed)"));
        else
            Log.info("Follow user failed with error: " + result.error());
    }

    private static void followers(String[] args, RestShortsClient client) {
        if (args.length != 4) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestShortsClientClass followers shorts userId password");
            return;
        }
        String userId = args[2];
        String pwd = args[3];

        var result = client.followers(userId, pwd);

        if (result.isOK())
            Log.info("Followers: " + result.value());
        else
            Log.info("Get followers failed with error: " + result.error());
    }

    private static void like(String[] args, RestShortsClient client) {
        if (args.length != 6) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestShortsClientClass like shorts shortId userId like (true/false) password");
            return;
        }
        String shortId = args[2];
        String userId = args[3];
        boolean isLiked = Boolean.parseBoolean(args[4]);
        String pwd = args[5];

        var result = client.like(shortId, userId, isLiked, pwd);

        if (result.isOK())
            Log.info(String.format("Liked short %s: %s", shortId, isLiked ? "Yes" : "No"));
        else
            Log.info("Like short failed with error: " + result.error());
    }

    private static void likes(String[] args, RestShortsClient client) {
        if (args.length != 4) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestShortsClientClass likes shorts shortId password");
            return;
        }
        String shortId = args[2];
        String pwd = args[3];

        var result = client.likes(shortId, pwd);

        if (result.isOK())
            Log.info("Likes: " + result.value());
        else
            Log.info("Get likes failed with error: " + result.error());
    }

    private static void getFeed(String[] args, RestShortsClient client) {
        if (args.length != 4) {
            System.err.println(
                    "Use: java tukano.clients.rest.RestShortsClientClass getFeed shorts userId password");
            return;
        }
        String userId = args[2];
        String pwd = args[3];

        var result = client.getFeed(userId, pwd);

        if (result.isOK())
            Log.info("Feed: " + result.value());
        else
            Log.info("Get feed failed with error: " + result.error());
    }

}
