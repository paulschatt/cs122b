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
import java.sql.*;
import java.util.*;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type


        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String limit = request.getParameter("limit");
        String page = request.getParameter("page");
        String title = request.getParameter("title");
        StringTokenizer tokenizer;
        StringBuilder matchQuery = new StringBuilder();
        if(title != null && !title.isEmpty()){
            tokenizer = new StringTokenizer(title, " ");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (!token.isEmpty()) {
                    matchQuery.append("+").append(token).append("*");
                }
                if (tokenizer.hasMoreTokens()) {
                    matchQuery.append(" ");
                }
            }
        }
        System.out.println("Works");
        int offset = 0;
        if(page != null) {
            offset = (Integer.parseInt(page) - 1) * Integer.parseInt(limit);
        }

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        System.out.println("Works");
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try {

            // Declare our statement

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT m.title, m.id, m.year, m.director, m.price, ")
                    .append("r.rating AS average_rating, r.numVotes AS votes, ")
                    .append("GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC) AS genres, ")
                    .append("GROUP_CONCAT(DISTINCT g.id ORDER BY g.name ASC) AS genres_id, ")
                    .append("GROUP_CONCAT(DISTINCT s.name ORDER BY s.name ASC) AS stars, ")
                    .append("GROUP_CONCAT(DISTINCT s.id ORDER BY s.name ASC) AS stars_id ")
                    .append("FROM movies m ")
                    .append("LEFT JOIN ratings r ON m.id = r.movieId ")
                    .append("LEFT JOIN genres_in_movies gim ON m.id = gim.movieId ")
                    .append("LEFT JOIN genres g ON gim.genreId = g.id ")
                    .append("LEFT JOIN stars_in_movies sim ON m.id = sim.movieId ")
                    .append("LEFT JOIN stars s ON sim.starId = s.id ")
                    .append("WHERE 1=1 ");

            List<String> params = new ArrayList<>();

            if (title != null && !title.isEmpty()) {
                sql.append("AND MATCH(m.title) AGAINST ('").append(matchQuery.toString()).append("' IN BOOLEAN MODE) ");

            }

            if (year != null && !year.isEmpty()) {
                sql.append("AND m.year = ? ");
                params.add(year);
            }

            if (director != null && !director.isEmpty()) {
                sql.append("AND m.director LIKE ? ");
                params.add("%" + director + "%");
            }

            if (star != null && !star.isEmpty()) {
                System.out.println("star in query");
                sql.append("AND s.name LIKE ? ");
                params.add("%" + star + "%");
            }

            sql.append("GROUP BY m.id ORDER BY m.title ASC ");
            sql.append("LIMIT ? ");
            sql.append("OFFSET ?");

            System.out.println("Works");
            PreparedStatement statement = conn.prepareStatement(sql.toString());
            System.out.println(statement.toString());
            int i;
            for (i = 0; i < params.size(); i++) {
                statement.setString(i + 1, params.get(i));
            }
            i++;
            statement.setInt(i++, Integer.parseInt(limit));
            statement.setInt(i++, offset);

            ResultSet rs = statement.executeQuery();


            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                System.out.println("got result");
                // get a movie from result set
                // Retrieve data from the ResultSet
                // Retrieve movie information from the ResultSet
                String movie_title = rs.getString("title");
                String movie_id = rs.getString("id");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_price = rs.getString("price");
                String movie_rating = rs.getString("average_rating");
                String movie_votes = rs.getString("votes");

                // Handle potential null or empty values for genres
                String movie_genres = rs.getString("genres");
                String[] genres_array = {};
                if (movie_genres != null && !movie_genres.trim().isEmpty()) {
                    genres_array = movie_genres.split(",\\s*");
                }

                // Handle potential null or empty values for genres_id
                String genres_id = rs.getString("genres_id");
                String[] genres_id_array = {};
                if (genres_id != null && !genres_id.trim().isEmpty()) {
                    genres_id_array = genres_id.split(",\\s*");
                }

                // Handle potential null or empty values for stars
                String movie_stars = rs.getString("stars");
                String[] star_array = {};
                if (movie_stars != null && !movie_stars.trim().isEmpty()) {
                    star_array = movie_stars.split(",\\s*");
                }

                // Handle potential null or empty values for stars_id
                String stars_id = rs.getString("stars_id");
                String[] stars_id_array = {};
                if (stars_id != null && !stars_id.trim().isEmpty()) {
                    stars_id_array = stars_id.split(",\\s*");
                }




                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_price", movie_price);
                jsonObject.addProperty("movie_rating", movie_rating != null ? movie_rating : "");
                jsonObject.addProperty("movie_votes", movie_votes);



                JsonArray genresJsonArray = new JsonArray();
                Arrays.stream(genres_array).forEach(genresJsonArray::add);
                jsonObject.add("movie_genres", genresJsonArray); // Use add(), not addProperty()

                // Create a JsonArray for genre IDs
                JsonArray genresIDJsonArray = new JsonArray();
                Arrays.stream(genres_id_array).forEach(genresIDJsonArray::add);
                jsonObject.add("genres_id", genresIDJsonArray); // Use add(), not addProperty()

                // Create a JsonArray for stars
                JsonArray starsJsonArray = new JsonArray();
                Arrays.stream(star_array).forEach(starsJsonArray::add);
                jsonObject.add("movie_stars", starsJsonArray); // Use add(), not addProperty()

                // Create a JsonArray for stars IDs
                JsonArray starsIDJsonArray = new JsonArray();
                Arrays.stream(stars_id_array).forEach(starsIDJsonArray::add);
                jsonObject.add("stars_id", starsIDJsonArray); // Use add(), not addProperty()


                // Add the JsonObject to jsonArray (assuming jsonArray is declared earlier)
                jsonArray.add(jsonObject);

                // Output the JSON object
                System.out.println(jsonObject.toString());
                System.out.println(title);
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