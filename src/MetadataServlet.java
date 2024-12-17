import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.out;

@WebServlet(name = "MetadataServlet", urlPatterns = "/api/metadata")
public class MetadataServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject responseJson = new JsonObject();
        response.setContentType("application/json");
        PrintWriter printer = response.getWriter();

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            JsonArray tablesArray = new JsonArray();

            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");

                    JsonObject tableObject = new JsonObject();
                    tableObject.addProperty("tableName", tableName);

                    JsonArray columnsArray = new JsonArray();
                    Set<String> addedColumns = new HashSet<>();
                    try (ResultSet columns = metaData.getColumns(null, null, tableName, "%")) {
                        while (columns.next()) {
                            String columnName = columns.getString("COLUMN_NAME");
                            if (!addedColumns.contains(columnName)) {
                                JsonObject columnObject = new JsonObject();
                                columnObject.addProperty("columnName", columns.getString("COLUMN_NAME"));
                                columnObject.addProperty("dataType", columns.getString("TYPE_NAME"));
                                columnsArray.add(columnObject);
                                addedColumns.add(columnName);
                            }
                        }
                    }

                    tableObject.add("columns", columnsArray); // Add columns array to the table object
                    tablesArray.add(tableObject); // Add table object to the tables array
                }
            }

            responseJson.add("tables", tablesArray); // Add tables array to the response JSON object
            printer.print(responseJson);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.print(jsonObject);

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

    }
}
