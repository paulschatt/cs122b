import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;


// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private Connection conn;
    public void init(ServletConfig config) {
        try {
            dbconnector dbc = new dbconnector();
            conn = dbc.getReadConnection();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT m.title, m.id, m.year, m.director, m.price, r.rating as average_rating, "
                    + "r.numVotes as votes "
                    + "FROM movies m "
                    + "LEFT JOIN ratings r ON m.id = r.movieId "
                    + "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId "
                    + "LEFT JOIN genres g ON gim.genreId = g.id "
                    + "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId "
                    + "LEFT JOIN stars s ON sim.starId = s.id "
                    + "WHERE m.id = ? "
                    + "GROUP BY m.id;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_title = rs.getString("title");
                String movie_id = rs.getString("id");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_price = rs.getString("price");
                String movie_rating = rs.getString("average_rating");
                String movie_votes = rs.getString("votes");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_price", movie_price);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating != null ? movie_rating : "");
                jsonObject.addProperty("movie_votes", movie_votes);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();
            decorateMoviesWithStars(jsonArray, conn);
            decorateMoviesWithGenres(jsonArray, conn);
            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
    protected void decorateMoviesWithStars(JsonArray movies, Connection conn) throws SQLException {
        String query = "SELECT s.id, s.name, COUNT(m.id) as num_movies FROM movies m\n" +
                "JOIN stars_in_movies sim ON m.id = sim.movieId\n" +
                "JOIN stars s ON sim.starId = s.id\n" +
                "WHERE sim.movieId = ? " +
                "GROUP BY s.id " +
                "ORDER BY num_movies DESC, s.name ASC;";
        try {
            for (int i = 0; i < movies.size(); i++) {
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, movies.get(i).getAsJsonObject().get("movie_id").getAsString());
                ResultSet rs = statement.executeQuery();
                JsonArray starsJsonArray = new JsonArray();
                JsonArray starsIdJsonArray = new JsonArray();
                while(rs.next()) {

                    String movie_stars = rs.getString("name");
                    String stars_id = rs.getString("id");

                    starsJsonArray.add(movie_stars != null ? movie_stars : "");
                    starsIdJsonArray.add(stars_id != null ? stars_id : "");

                }
                movies.get(i).getAsJsonObject().add("movie_stars", starsJsonArray);
                movies.get(i).getAsJsonObject().add("stars_id", starsIdJsonArray);
                rs.close();
                statement.close();
            }
        }catch(SQLException e){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            System.out.print(jsonObject);
        }
    }
    protected void decorateMoviesWithGenres(JsonArray movies, Connection conn) throws SQLException {
        String query = "SELECT g.id, g.name FROM movies m\n" +
                "JOIN genres_in_movies gim ON m.id = gim.movieId\n" +
                "JOIN genres g ON gim.genreId = g.id\n" +
                "WHERE gim.movieId = ? "+
                "ORDER BY g.name ASC;";

        for(int i = 0; i < movies.size(); i++){
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, movies.get(i).getAsJsonObject().get("movie_id").getAsString());
            ResultSet rs = statement.executeQuery();
            JsonArray genresJsonArray = new JsonArray();
            JsonArray genresIDJsonArray = new JsonArray();
            while(rs.next()){
                String id = rs.getString("id");
                String name = rs.getString("name");

                genresJsonArray.add(name != null ? name : "");
                genresIDJsonArray.add(id != null ? id : "");
            }
            rs.close();
            statement.close();
            movies.get(i).getAsJsonObject().add("movie_genres", genresJsonArray);
            movies.get(i).getAsJsonObject().add("genres_id", genresIDJsonArray); // Use add(), not addProperty()
        }
    }

}