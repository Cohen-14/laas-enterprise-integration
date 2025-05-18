package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class SelledProduct {
    public Long id;
    public String code;
    public Double unitprice;
    public String description;

    public SelledProduct() {
    }

    public SelledProduct(String code, Double unitprice, String description) {
        this.code = code;
        this.unitprice = unitprice;
        this.description = description;
    }
    public SelledProduct(Long id, String code, Double unitprice, String description) {
        this.id = id;
        this.code = code;
        this.unitprice = unitprice;
        this.description = description;
    }

    private static SelledProduct from(Row row) {
        return new SelledProduct(row.getLong("id"), row.getString("code"), row.getDouble("unitprice"),
                row.getString("description"));
    }

    public static Multi<SelledProduct> findAll(MySQLPool client) {
        return client.query("SELECT id, code, unitprice, description FROM selled_products ORDER BY id ASC")
                .execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(SelledProduct::from);
    }

    public static Uni<SelledProduct> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, code, unitprice, description FROM selled_products WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Boolean> save(MySQLPool client, String code_R, Double unitprice_R, String description_R) {
        return client.preparedQuery(
                "INSERT INTO selled_products(code, unitprice, description) VALUES (?,?,?)")
                .execute(Tuple.of(code_R, unitprice_R, description_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }

    public static Uni<Boolean> update(MySQLPool client, Long id_R, String code_R, Double unitprice_R,
            String description_R) {
        return client.preparedQuery(
                "UPDATE selled_products SET code = ?, unitprice = ?, description = ? WHERE id = ?")
                .execute(Tuple.of(code_R, unitprice_R, description_R, id_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }

    public static Uni<Boolean> delete(MySQLPool client, Long id_R) {
        return client.preparedQuery("DELETE FROM selled_products WHERE id = ?").execute(Tuple.of(id_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }
}
