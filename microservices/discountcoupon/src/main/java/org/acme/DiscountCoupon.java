package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class DiscountCoupon {
    
    public Long id;
    public Double discountAmount;
    public String expirationDate;

    public DiscountCoupon() {
    }

    public DiscountCoupon(Long id, Double discountAmount, String expirationDate) {
        this.id = id;
        this.discountAmount = discountAmount;
        this.expirationDate = expirationDate;
    }

    public DiscountCoupon(Double discountAmount, String expirationDate) {
        this.discountAmount = discountAmount;
        this.expirationDate = expirationDate;
    }

    private static DiscountCoupon from(Row row) {
        return new DiscountCoupon(row.getLong("id"),
                row.getDouble("discountAmount"), row.getString("expiration_date"));
    }

    public static Multi<DiscountCoupon> findAll(MySQLPool client) {
        return client.query("SELECT id, discountAmount, expiration_date FROM discount_coupons ORDER BY id ASC")
                .execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(DiscountCoupon::from);
    }

    public static Uni<DiscountCoupon> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, discountAmount, expiration_date FROM discount_coupons WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Boolean> save(MySQLPool client, Double discountAmount_R, String expirationDate_R) {
        return client.preparedQuery(
                "INSERT INTO discount_coupons(discountAmount, expiration_date) VALUES (?,?)")
                .execute(Tuple.of(discountAmount_R, expirationDate_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }

    public static Uni<Boolean> delete(MySQLPool client, Long id_R) {
        return client.preparedQuery("DELETE FROM discount_coupons WHERE id = ?").execute(Tuple.of(id_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }

    public static Uni<Boolean> update(MySQLPool client, Long id_R, Double discountAmount_R, String expirationDate_R) {
        return client.preparedQuery(
                "UPDATE discount_coupons SET discountAmount = ?, expiration_date = ? WHERE id = ?")
                .execute(Tuple.of(discountAmount_R, expirationDate_R, id_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    } 
}
