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

@Path("Discountcoupon")
public class DiscountCouponResource {
    
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
        client.query("DROP TABLE IF EXISTS discount_coupons").execute()
        .flatMap(r -> client.query("CREATE TABLE discount_coupons (id SERIAL PRIMARY KEY, discountAmount DOUBLE NOT NULL, expiration_date TEXT NOT NULL)").execute())
        .flatMap(r -> client.query("INSERT INTO discount_coupons (discountAmount,expiration_date) VALUES ('10.0','2023-12-31')").execute())
        .await().indefinitely();
    }

    @GET
    public Multi<DiscountCoupon> get() {
        return DiscountCoupon.findAll(client);
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(@PathParam("id") Long id) {
        return DiscountCoupon.findById(client, id)
                .onItem().transform(discountCoupon -> discountCoupon != null ? Response.ok(discountCoupon) : Response.status(Response.Status.NOT_FOUND)) 
                .onItem().transform(ResponseBuilder::build); 
    }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> create(DiscountCoupon discountCoupon) {
        return discountCoupon.save(client, discountCoupon.discountAmount, discountCoupon.expirationDate)
                .onItem().transform(id -> URI.create("/discountcoupon/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> delete (Long id) {
        return DiscountCoupon.delete(client, id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
    @PUT
    @Path("{id}/{discountAmount}/{expirationDate}")
    public Uni<Response> update(Long id, Double discountAmount, String expirationDate) {
        return DiscountCoupon.update(client, id, discountAmount, expirationDate)
                .onItem().transform(updated -> updated ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
}
