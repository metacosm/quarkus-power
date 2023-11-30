package io.quarkiverse.power.runtime;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.github.metacosm.power.SensorMetadata;
import io.smallrye.mutiny.Multi;

@Path("/power")
public interface PowerServerClient {
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Path("{pid}")
    Multi<String> powerFor(@PathParam("pid") long pid);

    @GET
    @Path("metadata")
    SensorMetadata metadata();
}
