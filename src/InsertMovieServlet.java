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



@WebServlet(name = "InsertMovieServlet", urlPatterns = "/api/insert-movie")
public class InsertMovieServlet extends HttpServlet {
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

        System.out.println("Works");
        String movieName = request.getParameter("movieName");
        String director = request.getParameter("director");
        String id = movieName.hashCode() + "";
        System.out.println(id);
        String year = request.getParameter("year");
        Integer yearInt = (year != null && !year.isEmpty()) ? Integer.parseInt(year) : null;

        System.out.println("Works");

        if (movieName.trim().isEmpty() || director.trim().isEmpty()) {
            return;
        }

        try {
            String insertQuery = "INSERT INTO movies (id, title, year, director, price) VALUES (?, ?, ?, ?, 1)";
            PreparedStatement statement = conn.prepareStatement(insertQuery);// Set the new star ID
            statement.setString(1, id);
            statement.setString(2, movieName);// Set the star name
            if (year != null) {
                statement.setInt(3, yearInt); // Set the birth year if provided
            } else {
                statement.setNull(3, Types.INTEGER); // Set NULL if no birth year is provided
            }
            statement.setString(4, director);
            System.out.println("Works");
            int rowsAffected = statement.executeUpdate();

            JsonObject json = new JsonObject();
            json.addProperty("success", "added new movie");
            out.write(json.toString());
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