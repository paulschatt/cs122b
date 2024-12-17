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


@WebServlet(name = "AllGenresServlet", urlPatterns = "/api/all-genres")
public class AllGenresServlet extends HttpServlet{
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            String query = "SELECT DISTINCT id, name FROM genres ORDER BY name ASC;";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                String genreId = rs.getString("id");
                String genreName = rs.getString("name");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("genreId", genreId);
                jsonObject.addProperty("genreName", genreName);
                System.out.println(jsonObject.toString());
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();
            out.write(jsonArray.toString());
            response.setStatus(200);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            out.close();
        }
    }
}

