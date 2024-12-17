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


@WebServlet(name="SingleStartingLetterServlet", urlPatterns ="/api/single-starting-letter")
public class SingleStartingletterServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
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
        String chr = request.getParameter("chr");
        request.getServletContext().log("getting movie title starting letter: " + chr);
        PrintWriter out = response.getWriter();
        try {
            PreparedStatement statement;
            if (chr.equals("*")) {
                String query = "SELECT m.title, m.id, m.director, m.year, m.price " +
                        "FROM movies m " +
                        "WHERE m.title NOT LIKE 'A%' " +
                        "AND m.title NOT LIKE 'B%' " +
                        "AND m.title NOT LIKE 'C%' " +
                        "AND m.title NOT LIKE 'D%' " +
                        "AND m.title NOT LIKE 'E%' " +
                        "AND m.title NOT LIKE 'F%' " +
                        "AND m.title NOT LIKE 'G%' " +
                        "AND m.title NOT LIKE 'H%' " +
                        "AND m.title NOT LIKE 'I%' " +
                        "AND m.title NOT LIKE 'J%' " +
                        "AND m.title NOT LIKE 'K%' " +
                        "AND m.title NOT LIKE 'L%' " +
                        "AND m.title NOT LIKE 'M%' " +
                        "AND m.title NOT LIKE 'N%' " +
                        "AND m.title NOT LIKE 'O%' " +
                        "AND m.title NOT LIKE 'P%' " +
                        "AND m.title NOT LIKE 'Q%' " +
                        "AND m.title NOT LIKE 'R%' " +
                        "AND m.title NOT LIKE 'S%' " +
                        "AND m.title NOT LIKE 'T%' " +
                        "AND m.title NOT LIKE 'U%' " +
                        "AND m.title NOT LIKE 'V%' " +
                        "AND m.title NOT LIKE 'W%' " +
                        "AND m.title NOT LIKE 'X%' " +
                        "AND m.title NOT LIKE 'Y%' " +
                        "AND m.title NOT LIKE 'Z%' " +
                        "AND m.title NOT LIKE '1%' " +
                        "AND m.title NOT LIKE '2%' " +
                        "AND m.title NOT LIKE '3%' " +
                        "AND m.title NOT LIKE '4%' " +
                        "AND m.title NOT LIKE '5%' " +
                        "AND m.title NOT LIKE '6%' " +
                        "AND m.title NOT LIKE '7%' " +
                        "AND m.title NOT LIKE '8%' " +
                        "AND m.title NOT LIKE '9%' " +
                        "AND m.title NOT LIKE '0%' " +
                        "ORDER BY m.title;";
                statement = conn.prepareStatement(query);
            } else {
                String query = "SELECT m.title, m.id, m.director, m.year, m.price FROM movies m " +
                        "WHERE m.title LIKE ?;";
                statement = conn.prepareStatement(query);
                chr = chr + "%";
                statement.setString(1, chr);
            }
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieDirector = rs.getString("director");
                String movieYear = rs.getString("year");
                String price = rs.getString("price");

                JsonObject json = new JsonObject();
                json.addProperty("movieId", movieId);
                json.addProperty("movieTitle", movieTitle);
                json.addProperty("year", movieYear);
                json.addProperty("director", movieDirector);
                json.addProperty("price", price);

                jsonArray.add(json);
            }
            statement.close();
            rs.close();
            decorateMoviesWithGenres(jsonArray, conn);
            decorateMoviesWithStars(jsonArray, conn);
            out.write(jsonArray.toString());
            response.setStatus(200);
        } catch (SQLException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
            throw new RuntimeException(e);
        } finally{
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

