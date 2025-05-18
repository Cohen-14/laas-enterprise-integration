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

@Path("couponanalysis")
public class CouponAnalysisResource {

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
        client.query("DROP TABLE IF EXISTS coupon_analysis").execute()
        .flatMap(r -> client.query("CREATE TABLE coupon_analysis (id SERIAL PRIMARY KEY, idCoupon BIGINT NOT NULL, prompt TEXT NOT NULL, response TEXT NOT NULL)").execute())
        .flatMap(r -> client.query("INSERT INTO coupon_analysis (idCoupon, prompt, response) VALUES (1, 'prompt1', 'response1')").execute())
        .await().indefinitely();
    }

    @GET
    public Multi<CouponAnalysis> get() {
        return CouponAnalysis.findAll(client);
    }
    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return CouponAnalysis.findById(client, id)
                .onItem().transform(couponAnalysis -> couponAnalysis != null ? Response.ok(couponAnalysis) : Response.status(Response.Status.NOT_FOUND)) 
                .onItem().transform(ResponseBuilder::build); 
    }

    @POST
    public Uni<Response> create(CouponAnalysis couponAnalysis) {
        return couponAnalysis.save(client, couponAnalysis.idCoupon, couponAnalysis.prompt, couponAnalysis.response)
                .onItem().transform(id -> URI.create("/couponanalysis/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }

    @PUT
    @Path("{id}/{coupon_id}/{prompt}/{response}")
    public Uni<Response> update(@PathParam("id") Long id, Long coupon_id ,String prompt, String response) {
        return CouponAnalysis.update(client, id, coupon_id, prompt, response)
                .onItem().transform(updated -> updated ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build());
    }
    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return CouponAnalysis.delete(client, id)
                .onItem().transform(deleted -> deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build());
    }
}