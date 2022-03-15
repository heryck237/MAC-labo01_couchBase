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
        QueryResult result = cluster.query(
                "SELECT M.imdb.id AS imdb_id, M.tomatoes.viewer.rating AS tomatoes_rating, M.imdb.rating AS imdb_rating\n" +
                        "FROM `mflix-sample`._default.movies M\n" +
                        "WHERE M.tomatoes.viewer.rating <> 0 AND (M.imdb.rating - M.tomatoes.viewer.rating) > 7;"
        );
        return result.rowsAs(JsonObject.class);
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
        QueryResult result = cluster.query(
                "SELECT DISTINCT C.name, COUNT(*) AS cnt\n" +
                        "FROM `mflix-sample`._default.comments C\n" +
                        "GROUP BY C.name\n" +
                        "ORDER BY COUNT(*) DESC\n" +
                        "LIMIT 10;"
        );
        return result.rowsAs(JsonObject.class);
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
        QueryResult result = cluster.query(
                "SELECT M.imdb.id AS imdb_id, M.imdb.rating, M.`cast`\n" +
                        "FROM `mflix-sample`._default.movies M\n" +
                        "WHERE M.imdb.rating > 8\n" +
                        "AND \"" + actor + "\" WITHIN M.`cast`;"
        );
        return result.rowsAs(JsonObject.class);
    }

    public List<JsonObject> plentifulDirectors() {
        QueryResult result = cluster.query(
                "with all_director AS (select distinct directors as name, count(*) nb_film\n" +
                        " FROM `mflix-sample`._default.movies\n" +
                        "UNNEST directors\n" +
                        "GROUP BY directors)\n" +

                        "SELECT all_director.name director_name, all_director.nb_film count_film\n" +
                        "from all_director\n" +
                        "WHERE all_director.nb_film > 30;"
        );
        return result.rowsAs(JsonObject.class);
    }

    public List<JsonObject> confusingMovies() {
        QueryResult result = cluster.query(
                "SELECT M._id AS movie_id, M.title\n" +
                        "FROM `mflix-sample`._default.movies M\n" +
                        "WHERE ARRAY_LENGTH(M.directors) > 20;"
        );
        return result.rowsAs(JsonObject.class);
    }

    public List<JsonObject> commentsOfDirector1(String director) {
        QueryResult result = cluster.query(
                "SELECT M._id AS movie_id, C.text \n" +
                        "FROM `mflix-sample`._default.movies M\n" +
                        "INNER JOIN `mflix-sample`._default.comments C\n" +
                        "ON M._id = C.movie_id\n" +
                        "WHERE \"" + director + "\" WITHIN M.directors");
        return result.rowsAs(JsonObject.class);
    }

    public List<JsonObject> commentsOfDirector2(String director) {
        QueryResult result = cluster.query(
                "SELECT M._id AS movie_id, C.text \n" +
                        "FROM `mflix-sample`._default.movies M\n" +
                        "INNER JOIN `mflix-sample`._default.comments C\n" +
                        "ON M._id = C.movie_id AND \"" + director + "\" WITHIN M.directors");

        return result.rowsAs(JsonObject.class);

    }

    // Returns the number of documents updated.
    public long removeEarlyProjection(String movieId) {
        QueryResult result = cluster.query(
                "UPDATE mflix-sample._default.theaters\n" +
                        "SET schedule = (ARRAY s for s IN schedule\n" +
                        "WHEN s.movieId != \"" + movieId + "\" OR s.hourBegin >= '18:00:00' END)"+
                        "QueryOptions.queryOptions().metrics(true);"
        );
        return result.metaData().metrics().get().mutationCount();
    }

    public List<JsonObject> nightMovies() {
        QueryResult result = cluster.query(
                "WITH early_projection AS (SELECT DISTINCT sched.movieId id, sched.hourBegin hour\n" +
                        "FROM `mflix-sample`._default.theaters\n" +
                        "UNNEST schedule as sched\n" +
                        "WHERE sched.hourBegin < '18:00:00')\n" +
                        "SELECT M._id movie_id, M.title \n" +
                        "FROM `mflix-sample`._default.movies M\n"+
                        "WHERE M._id NOT IN (select early_projection.id);"
        );
        return result.rowsAs(JsonObject.class);

    }


}
