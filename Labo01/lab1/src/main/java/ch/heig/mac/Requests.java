package ch.heig.mac;

import java.util.List;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;


public class Requests {
    private final Cluster cluster;

    public Requests(Cluster cluster) {
        this.cluster = cluster;
    }

    public List<String> getCollectionNames() {
        QueryResult result = cluster.query(
                "SELECT RAW r.name\n" +
                        "FROM system:keyspaces r\n" +
                        "WHERE r.`bucket` = \"mflix-sample\";"
        );
        return result.rowsAs(String.class);
    }

    public List<JsonObject> inconsistentRating() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    public List<JsonObject> hiddenGem() {
        QueryResult result = cluster.query(
        "SELECT M.title\n" +
        "FROM `mflix-sample`._default.movies M\n" +
        "WHERE M.tomatoes.critic.rating = 10 AND M.tomatoes.viewer.rating IS MISSING;"
    );
        return result.rowsAs(JsonObject.class);
    }

    public List<JsonObject> topReviewers() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    public List<String> greatReviewers() {
        QueryResult result = cluster.query(
                "SELECT C.name\n" +
        "FROM `mflix-sample`._default.comments C\n" +
        "GROUP BY C.name\n" +
        "HAVING COUNT(*) > 300;"
        );
        return result.rowsAs(String.class);
    }

    public List<JsonObject> bestMoviesOfActor(String actor) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    public List<JsonObject> plentifulDirectors() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    public List<JsonObject> confusingMovies() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    public List<JsonObject> commentsOfDirector1(String director) {

        QueryResult result = cluster.query(
        "SELECT C.movie_id, C.text \n" +
        "FROM `mflix-sample`._default.movies M\n" +
        "INNER JOIN `mflix-sample`._default.comments C\n" +
        "ON M._id = C.movie_id\n" +
                "WHERE '" + director + "' WITHIN M.directors");
        return result.rowsAs(JsonObject.class);
    }

    public List<JsonObject> commentsOfDirector2(String director) {
        QueryResult result = cluster.query(
                "SELECT C.movie_id, C.text \n" +
                        "FROM `mflix-sample`._default.movies M\n" +
                        "INNER JOIN `mflix-sample`._default.comments C\n" +
                        "ON M._id = C.movie_id AND '" + director + "' WITHIN M.directors");

        return result.rowsAs(JsonObject.class);

    }

    // Returns the number of documents updated.
    public long removeEarlyProjection(String movieId) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    public List<JsonObject> nightMovies() {
        QueryResult result = cluster.query(
                "SELECT DISTINCT T.movie_id, M.title\n" +
                         "FROM `mflix-sample`._default.movies M \n" +
                        "INNER JOIN `mflix-sample`._default.theaters T\n" +
                         "ON T.movie_id = M._id\n" +
                         "WHERE T.movie_id NOT IN (" +
                        "SELECT DISTINCT Th.movieId\n" +
                        "FROM `mflix-sample`._default.theaters Th\n" +
                        "WHERE T.hourBegin < '18:00:00'");
        return result.rowsAs(JsonObject.class);

    }


}
