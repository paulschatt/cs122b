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
import java.util.StringTokenizer;

@WebServlet(name="AutocompleteServlet", urlPatterns="/api/autocomplete")
public class AutoCompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String searchTerm = request.getParameter("searchTerm");

        StringTokenizer tokenizer = new StringTokenizer(searchTerm, " ");
        StringBuilder matchQuery = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (!token.isEmpty()) {
                matchQuery.append("+").append(token).append("*");
            }
            if(tokenizer.hasMoreTokens()){
                matchQuery.append(" ");
            }
        }
        try {
            String sql = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST ('" + matchQuery.toString() + "' IN BOOLEAN MODE) LIMIT 10;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                String id = rs.getString("id");
                String title  = rs.getString("title");

                jsonArray.add(generateJsonObject(id, title));
            }
            out.write(jsonArray.toString());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonObject generateJsonObject(String movieId, String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", movieId);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }
}
