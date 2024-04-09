package tukano.clients.rest;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import tukano.api.java.Result;
import utils.Sleep;

import java.util.function.Supplier;

import static tukano.api.java.Result.ErrorCode;
import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;
import static tukano.clients.rest.RestBlobsClient.getErrorCodeFrom;

public class RestClient {
    protected static final int MAX_RETRIES = 10;
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

    protected <T> Result<T> toJavaResult(Response r, Class<T> entityType) {
        try {
            var status = r.getStatusInfo().toEnum();
            if (status == Response.Status.OK && r.hasEntity())
                return ok(r.readEntity(entityType));
            else if (status == Response.Status.NO_CONTENT) return ok();

            return error(getErrorCodeFrom(status.getStatusCode()));
        } finally {
            r.close();
        }
    }
}
