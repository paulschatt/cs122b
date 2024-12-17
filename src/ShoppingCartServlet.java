import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import com.google.gson.JsonArray;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/cart.
 */
@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {
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
            HttpSession session = request.getSession();
            Map<String, CartItem> cart;
            cart = (Map<String, CartItem>) session.getAttribute("cart");
            String json = convertCartToJSON(cart).toString();
            out.write(json);
            response.setStatus(200);
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        String movieId = request.getParameter("movieId");
        int quantity;
        float price;
        try {
            Map<String, CartItem> cart;
            cart = (Map<String, CartItem>) session.getAttribute("cart");

            if (cart == null) {
                cart = new HashMap<>();
                session.setAttribute("cart", cart);
            }

            switch (action) {
                case "add":
                    quantity = Integer.valueOf(request.getParameter("quantity"));
                    price = Float.valueOf(request.getParameter("price"));
                    addToCart(movieId, quantity, price, cart);
                    break;
                case "remove":
                    removeFromCart(movieId, cart);
                    break;
                case "update":
                    quantity = Integer.valueOf(request.getParameter("quantity"));
                    updateQuantity(movieId, quantity, cart);
                    break;
            }
            String json = convertCartToJSON(cart).toString();
            out.write(json);
        }
        catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally{
            out.close();
        }
    }
    protected JsonArray convertCartToJSON(Map<String, CartItem> cart) {

        JsonArray jsonArray = new JsonArray();
        try {
            for (Map.Entry<String, CartItem> entry : cart.entrySet()) {
                JsonObject jsonObject = new JsonObject();
                String movieId = entry.getKey();
                CartItem item = entry.getValue();
                jsonObject.addProperty("movieId", movieId);
                jsonObject.addProperty("quantity", item.getQuantity());
                jsonObject.addProperty("price", item.getPrice());

                String query = "SELECT title FROM movies WHERE id = ?;";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, movieId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            String title = rs.getString("title");
                            jsonObject.addProperty("title", title);
                            rs.close();
                        } else {
                            jsonObject.addProperty("title", "Unknown");
                        }
                    }
                    finally{
                        statement.close();
                    }

                }
                jsonArray.add(jsonObject);
            }
        } catch (SQLException e) {
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("errorMessage", e.getMessage());
            jsonArray.add(errorObject);
        }
        return jsonArray;
    }

    private void addToCart(String movieId, int quantity, float price, Map<String, CartItem> cart) {
        CartItem item = cart.get(movieId);
        if (item == null) {
            item = new CartItem(quantity, price);
            cart.put(movieId, item);
        }
        else{
            item.setQuantity(item.getQuantity() + quantity);
        }
    }

    private void removeFromCart(String movieId, Map<String, CartItem> cart) {
        cart.remove(movieId);
    }

    private void updateQuantity(String movieId, int quantity, Map<String, CartItem> cart) {
        CartItem item = cart.get(movieId);
        if (item == null) {
            return;
        }
        item.setQuantity(quantity);
    }
}



