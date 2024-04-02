package net.laprun.sustainability.power.quarkus.runtime;

import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.laprun.sustainability.power.SensorMetadata;

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
