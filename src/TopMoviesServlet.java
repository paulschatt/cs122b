import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Arrays;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import jakarta.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;




// This annotation maps this Java Servlet Class to a URL
@WebServlet(name = "/top-movies", urlPatterns = "/api/top-movies")
public class TopMoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Connection connection;
    public void init(ServletConfig config) {
        try {
            dbconnector dbc = new dbconnector();
            connection = dbc.getReadConnection();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set response mime type
        response.setContentType("application/json"); // Response mime type

        // Get the PrintWriter for writing response
        PrintWriter out = response.getWriter();

        try {
            // create database connection
            // declare statement
            Statement statement = connection.createStatement();
            // prepare query
            String query = "SELECT \n" +
                    "    m.title, m.id, m.year, m.director, m.price, r.rating as average_rating, r.numVotes as votes,\n" +
                    "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC), ',', 3) AS genres,\n" +
                    "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.id ORDER BY g.name ASC), ',', 3) AS genres_id,\n" +
                    "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name ASC), ',', 3) AS stars,\n" +
                    "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name ASC), ',', 3) AS stars_id\n" +
                    "FROM movies m\n" +
                    "JOIN ratings r ON m.id = r.movieId\n" +
                    "JOIN genres_in_movies gim ON m.id = gim.movieId\n" +
                    "JOIN genres g ON gim.genreId = g.id\n" +
                    "JOIN stars_in_movies sim ON m.id = sim.movieId\n" +
                    "JOIN stars s ON sim.starId = s.id\n" +
                    "GROUP BY m.id\n" +
                    "ORDER BY average_rating DESC\n" +
                    "LIMIT 20;";

            // execute query
            ResultSet rs = statement.executeQuery(query);
            JsonArray jsonArray = new JsonArray();
            // Iterate through rows of query result
            while (rs.next()) {
                // get a movie from result set
                // Retrieve data from the ResultSet
                String movie_title = rs.getString("title");
                String movie_id = rs.getString("id");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_price = rs.getString("price");
                String movie_rating = rs.getString("average_rating");
                String movie_votes = rs.getString("votes");
                String movie_genres = rs.getString("genres");
                String[] genres_array = movie_genres.split(",\\s*");
                String genres_id = rs.getString("genres_id");
                String[] genres_id_array = genres_id.split(",\\s*");
                String movie_stars = rs.getString("stars");
                String[] star_array = movie_stars.split(",\\s*");
                String stars_id = rs.getString("stars_id");
                String[] stars_id_array = stars_id.split(",\\s*");


                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_price", movie_price);
                jsonObject.addProperty("movie_rating", movie_rating);
                jsonObject.addProperty("movie_votes", movie_votes);
                // Create a JsonArray for genres
                JsonArray genresJsonArray = new JsonArray();
                Arrays.stream(genres_array).forEach(genresJsonArray::add);
                jsonObject.add("movie_genres", genresJsonArray); // Use add(), not addProperty()

                // Create a JsonArray for stars
                JsonArray starsJsonArray = new JsonArray();
                Arrays.stream(star_array).forEach(starsJsonArray::add);
                jsonObject.add("movie_stars", starsJsonArray); // Use add(), not addProperty()

                // Create a JsonArray for genre IDs
                JsonArray genresIDJsonArray = new JsonArray();
                Arrays.stream(genres_id_array).forEach(genresIDJsonArray::add);
                jsonObject.add("genres_id", genresIDJsonArray); // Use add(), not addProperty()

                // Create a JsonArray for stars IDs
                JsonArray starsIDJsonArray = new JsonArray();
                Arrays.stream(stars_id_array).forEach(starsIDJsonArray::add);
                jsonObject.add("stars_id", starsIDJsonArray); // Use add(), not addProperty()

                // Add the JsonObject to jsonArray (assuming jsonArray is declared earlier)
                jsonArray.add(jsonObject);

                // Output the JSON object
                System.out.println(jsonObject.toString());
            }

            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());


            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally {
            out.close();
        }
    }
}
