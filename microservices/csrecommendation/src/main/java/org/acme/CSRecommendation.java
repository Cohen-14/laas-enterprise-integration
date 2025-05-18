package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class CSRecommendation {
    
    public Long id;
    public String recommendation;

    public CSRecommendation() {
    }

    public CSRecommendation(Long id, String recommendation) {
        this.id = id;
        this.recommendation = recommendation;
    }
    public CSRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    private static CSRecommendation from(Row row) {
        return new CSRecommendation(row.getLong("id"), row.getString("recommendation"));
    }
    public static Multi<CSRecommendation> findAll(MySQLPool client) {
        return client.query("SELECT id, recommendation FROM cs_recommendations ORDER BY id ASC")
                .execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(CSRecommendation::from);
    }
    public static Uni<CSRecommendation> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, recommendation FROM cs_recommendations WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }
    public Uni<Boolean> save(MySQLPool client, String recommendation_R) {
        return client.preparedQuery(
                "INSERT INTO cs_recommendations(recommendation) VALUES (?)")
                .execute(Tuple.of(recommendation_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }
    public static Uni<Boolean> delete(MySQLPool client, Long id_R) {
        return client.preparedQuery("DELETE FROM cs_recommendations WHERE id = ?").execute(Tuple.of(id_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }
    public static Uni<Boolean> update(MySQLPool client, Long id_R, String recommendation_R) {
        return client.preparedQuery(
                "UPDATE cs_recommendations SET recommendation = ? WHERE id = ?")
                .execute(Tuple.of(recommendation_R, id_R))
                .onItem().transform(rowSet -> rowSet.rowCount() > 0);
    }
}
