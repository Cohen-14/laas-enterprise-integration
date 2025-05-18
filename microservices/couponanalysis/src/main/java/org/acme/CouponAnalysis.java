package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class CouponAnalysis {
    public Long id;
    public Long idCoupon;
    public String prompt;
    public String response;

    public CouponAnalysis() {
    }

    public CouponAnalysis(Long id, Long idCoupon, String prompt, String response) {
        this.id = id;
        this.idCoupon = idCoupon;
        this.prompt = prompt;
        this.response = response;
    }
    public CouponAnalysis(Long idCoupon, String prompt, String response) {
        this.idCoupon = idCoupon;
        this.prompt = prompt;
        this.response = response;
    }
    private static CouponAnalysis from(Row row) {
        return new CouponAnalysis(row.getLong("id"), row.getLong("idCoupon"), row.getString("prompt"), row.getString("response"));
    }
    public static Multi<CouponAnalysis> findAll(MySQLPool client) {
        return client.query("SELECT id, idCoupon, prompt, response FROM coupon_analysis ORDER BY id ASC")
                .execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(CouponAnalysis::from);
    }
    public static Uni<CouponAnalysis> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, idCoupon, prompt, response FROM coupon_analysis WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }
    public Uni<Boolean> save(MySQLPool client, Long idCoupon_R, String prompt_R, String response_R) {
        return client.preparedQuery(
                "INSERT INTO coupon_analysis(idCoupon, prompt, response) VALUES (?, ?, ?)")
                .execute(Tuple.of(idCoupon_R, prompt_R, response_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }
    public static Uni<Boolean> delete(MySQLPool client, Long id_R) {
        return client.preparedQuery("DELETE FROM coupon_analysis WHERE id = ?").execute(Tuple.of(id_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }
    public static Uni<Boolean> update(MySQLPool client, Long id_R, Long idCoupon_R, String prompt_R, String response_R) {
        return client.preparedQuery(
                "UPDATE coupon_analysis SET idCoupon = ?, prompt = ?, response = ? WHERE id = ?")
                .execute(Tuple.of(idCoupon_R, prompt_R, response_R, id_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }
}
