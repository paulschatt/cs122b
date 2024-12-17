import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "SingleGenreServlet", urlPatterns = "/api/single-genre")
public class SingleGenreServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime

        String id = request.getParameter("id");
        request.getServletContext().log("getting genre id: " + id);
        PrintWriter out = response.getWriter();
        try{
            String query = "SELECT m.id, m.title, m.year, m.director, m.price FROM movies m " +
                    "JOIN genres_in_movies gim ON gim.movieID = m.id " +
                    "WHERE gim.genreId = ? " +
                    "ORDER BY m.title ASC;";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();
            while(rs.next()){
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String price = rs.getString("price");

                JsonObject json = new JsonObject();
                json.addProperty("movieId", movieId);
                json.addProperty("movieTitle", movieTitle);
                json.addProperty("year", year);
                json.addProperty("director", director);
                json.addProperty("price", price);

                jsonArray.add(json);
            }
            rs.close();
            statement.close();
            System.out.println("Before: " + jsonArray);
            decorateMoviesWithStars(jsonArray, conn);
            decorateMoviesWithGenres(jsonArray, conn);
            System.out.println("After: " + jsonArray);
            out.write(jsonArray.toString());
            response.setStatus(200);
        }
        catch (SQLException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally {
            out.close();
        }
    }
    protected void decorateMoviesWithStars(JsonArray movies, Connection conn) throws SQLException {
        String query = "SELECT s.id, s.name FROM movies m\n" +
                "JOIN stars_in_movies sim ON m.id = sim.movieId\n" +
                "JOIN stars s ON sim.starId = s.id\n" +
                "WHERE sim.movieId = ?\n"+
                "LIMIT 3;";
        try {
            for (int i = 0; i < movies.size(); i++) {
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, movies.get(i).getAsJsonObject().get("movieId").getAsString());
                ResultSet rs = statement.executeQuery();
                JsonArray starsJsonArray = new JsonArray();
                JsonArray starsIdJsonArray = new JsonArray();
                while(rs.next()) {

                    String movie_stars = rs.getString("name");
                    String stars_id = rs.getString("id");

                    starsJsonArray.add(movie_stars);
                    starsIdJsonArray.add(stars_id);

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
                "WHERE gim.movieId = ?\n" +
                "LIMIT 3;";

        for(int i = 0; i < movies.size(); i++){
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, movies.get(i).getAsJsonObject().get("movieId").getAsString());
            ResultSet rs = statement.executeQuery();
            JsonArray genresJsonArray = new JsonArray();
            JsonArray genresIDJsonArray = new JsonArray();
            while(rs.next()){
                String id = rs.getString("id");
                String name = rs.getString("name");

                genresJsonArray.add(name);
                genresIDJsonArray.add(id);
            }
            rs.close();
            statement.close();
            movies.get(i).getAsJsonObject().add("movie_genres", genresJsonArray);
            movies.get(i).getAsJsonObject().add("genres_id", genresIDJsonArray); // Use add(), not addProperty()
        }
    }
}
