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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.System.out;



@WebServlet(name = "InsertStarServlet", urlPatterns = "/api/insert-star")
public class InsertStarServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private Connection conn;
    public void init(ServletConfig config) {
        try {
            dbconnector dbc = new dbconnector();
            conn = dbc.getWriteConnection();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String starName = request.getParameter("starName");
        String birthYearParam = request.getParameter("birthYear");
        Integer birthYear = (birthYearParam != null && !birthYearParam.isEmpty()) ? Integer.parseInt(birthYearParam) : null;


        if (starName == null || starName.trim().isEmpty()) {
            return;
        }

        try {
            String insertQuery = "INSERT INTO stars (name, birthYear) VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(insertQuery);// Set the new star ID
            statement.setString(1, starName); // Set the star name
            if (birthYear != null) {
                statement.setInt(2, birthYear); // Set the birth year if provided
            } else {
                statement.setNull(2, Types.INTEGER); // Set NULL if no birth year is provided
            }

            int rowsAffected = statement.executeUpdate();

            JsonObject json = new JsonObject();
            json.addProperty("success", "added new star");
            out.write(json.toString());

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