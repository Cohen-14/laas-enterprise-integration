package org.acme;

import java.net.URI;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.MediaType;

@Path("csrecommendation")
public class CSRecommendationResource {

    @Inject
    io.vertx.mutiny.mysqlclient.MySQLPool client;
    
    @Inject
    @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") 
    boolean schemaCreate ;

    void config(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initdb();
        }
    }
    
    private void initdb() {
        // In a production environment this configuration SHOULD NOT be used
        client.query("DROP TABLE IF EXISTS CSRecommendation").execute()
        .flatMap(r -> client.query("CREATE TABLE CSRecommendation (id SERIAL PRIMARY KEY, name TEXT NOT NULL, description TEXT NOT NULL)").execute())
        .flatMap(r -> client.query("INSERT INTO CSRecommendation (recommendation) VALUES ('recommendation1')").execute())
        .await().indefinitely();
    }

    @GET
    public Multi<CSRecommendation> get() {
        return CSRecommendation.findAll(client);
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return CSRecommendation.findById(client, id)
                .onItem().transform(csRecommendation -> csRecommendation != null ? Response.ok(csRecommendation) : Response.status(Response.Status.NOT_FOUND)) 
                .onItem().transform(ResponseBuilder::build); 
    }

    @POST
    public Uni<Response> create(CSRecommendation csRecommendation) {
        return csRecommendation.save(client, csRecommendation.recommendation)
                .onItem().transform(id -> URI.create("/csrecommendation/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(@PathParam("id") Long id, CSRecommendation csRecommendation) {
        return csRecommendation.update(client, id, csRecommendation.recommendation)
                .onItem().transform(updated -> updated ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return CSRecommendation.delete(client, id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
}
