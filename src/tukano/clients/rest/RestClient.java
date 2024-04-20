package tukano.clients.rest;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import tukano.api.java.Result;
import utils.Sleep;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

import static tukano.api.java.Result.ErrorCode;
import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;

public class RestClient {

    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;

    final URI serverURI;
    final Client client;
    final ClientConfig config;

    public RestClient(URI serverURI) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);
    }

    protected static final int MAX_RETRIES = 3;
    protected static final int RETRY_SLEEP = 1000;

    protected <T> Result<T> reTry(Supplier<Result<T>> func) {
        for (int i = 0; i < MAX_RETRIES; i++)
            try {
                return func.get();
            } catch (ProcessingException x) {
                Sleep.ms(RETRY_SLEEP);
            } catch (Exception x) {
                x.printStackTrace();
                return error(ErrorCode.INTERNAL_ERROR);
            }
        return error(ErrorCode.TIMEOUT);
    }

    protected Result<Void> toJavaResult(Response r) {
        return toJavaResult(r, Void.class);
    }

    protected <T> Result<List<T>> toJavaResultForList(Response r, Class<T> entityType) {
        try {
            var status = r.getStatusInfo().toEnum();
            if (status == Response.Status.OK && r.hasEntity())
                return ok(r.readEntity(new GenericType<List<T>>() {
                }));
            else if (status == Response.Status.NO_CONTENT)
                return ok();

            return error(getErrorCodeFrom(status.getStatusCode()));
        } finally {
            r.close();
        }
    }

    protected <T> Result<T> toJavaResult(Response r, Class<T> entityType) {
        try {
            var status = r.getStatusInfo().toEnum();
            if (status == Response.Status.OK && r.hasEntity())
                return ok(r.readEntity(entityType));
            else if (status == Response.Status.NO_CONTENT)
                return ok();

            return error(getErrorCodeFrom(status.getStatusCode()));
        } finally {
            r.close();
        }
    }

    public static Result.ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> Result.ErrorCode.OK;
            case 409 -> Result.ErrorCode.CONFLICT;
            case 403 -> Result.ErrorCode.FORBIDDEN;
            case 404 -> Result.ErrorCode.NOT_FOUND;
            case 400 -> Result.ErrorCode.BAD_REQUEST;
            case 500 -> Result.ErrorCode.INTERNAL_ERROR;
            case 501 -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
