import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;


@WebServlet(name="PaymentProcessingServlet", urlPatterns="/api/payment-processing")
public class PaymentProcessingServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject jsonObject = new JsonObject();

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        String username = user.getUsername();
        int userId = user.getUserId();

        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String creditcard = request.getParameter("creditcard");
        String date = request.getParameter("date");

        try {
            String query = "SELECT * FROM customers c " +
                    "JOIN creditcards cc " +
                    "ON c.firstName = cc.firstName AND c.lastName = cc.lastName " +
                    "WHERE c.email = ? " +
                    "AND cc.id = ? " +
                    "AND cc.expiration = ? " +
                    "AND cc.firstName = ? " +
                    "AND cc.lastName = ?;";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, creditcard);
            statement.setString(3, date);
            statement.setString(4, fname);
            statement.setString(5, lname);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                statement.close();
                rs.close();
                Map<String, CartItem> cart;
                cart = (Map<String, CartItem>) session.getAttribute("cart");
                String DBMessage = addSale(conn, userId, cart);
                jsonObject.addProperty("success", true);
                session.removeAttribute("cart");
                out.write(jsonObject.toString());
                response.setStatus(200);
            } else {
                out.write(jsonObject.toString());
                response.setStatus(400);
            }
        }catch(SQLException e){
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }finally{
            out.close();
        }
    }
    protected String addSale(Connection conn, int userId, Map<String, CartItem> cart) {
        JsonObject jsonObject = new JsonObject();
        String date = getFormattedDate();
        System.out.println(date);
        try {
            String query = "INSERT INTO sales(customerId, movieId, saleDate, quantity) VALUES(?, ?, ?, ?);";
            int counter = 0;
            for (Map.Entry<String, CartItem> entry : cart.entrySet()) {
                String movieId = entry.getKey();
                CartItem cartItem = entry.getValue();
                Integer quantity = cartItem.getQuantity();

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setInt(1, userId);
                statement.setString(2, movieId);
                statement.setString(3, date);
                statement.setInt(4, quantity);
                statement.executeUpdate();
                statement.close();
                counter = counter + 1;
            }

            jsonObject.addProperty("success: ", counter + " rows inserted into sales table");
        }
        catch (SQLException e) {
            System.out.println("Error lies in SQL Update");
            jsonObject.addProperty("errorMessage", e.getMessage());
        }
        cart = null;
        return jsonObject.toString();
    }
    protected String getFormattedDate(){
        String pattern = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(pattern);
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }
}




