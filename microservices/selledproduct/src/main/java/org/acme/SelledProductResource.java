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

@Path("selledproduct")
public class SelledProductResource {
    
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
        client.query("DROP TABLE IF EXISTS selled_products").execute()
        .flatMap(r -> client.query("CREATE TABLE selled_products (id SERIAL PRIMARY KEY, code TEXT NOT NULL, unitprice DOUBLE NOT NULL, description TEXT NOT NULL)").execute())
        .flatMap(r -> client.query("INSERT INTO selled_products (code,unitprice,description) VALUES ('P001','10.0','Product 1')").execute())
        .await().indefinitely();
    }
    
    @GET
    public Multi<SelledProduct> get() {
        return SelledProduct.findAll(client);
    }
    
    @GET
    @Path("{id}")
    public Uni<Response> getSingle(@PathParam("id") Long id) {
        return SelledProduct.findById(client, id)
                .onItem().transform(selledProduct -> selledProduct != null ? Response.ok(selledProduct) : Response.status(Response.Status.NOT_FOUND)) 
                .onItem().transform(ResponseBuilder::build); 
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> create(SelledProduct selledProduct) {
        return selledProduct.save(client, selledProduct.code, selledProduct.unitprice, selledProduct.description)
                .onItem().transform(id -> URI.create("/selledproduct/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return SelledProduct.delete(client, id)
                .onItem().transform(deleted -> deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("{id}/{code}/{unitprice}/{description}")
    public Uni<Response> update(@PathParam("id") Long id, @PathParam("code") String code,
            @PathParam("unitprice") Double unitprice, @PathParam("description") String description) {
        return SelledProduct.update(client, id, code, unitprice, description)
                .onItem().transform(updated -> updated ?Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
}
