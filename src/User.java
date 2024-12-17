/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;
    private final int userId;

    public User(String username, int userId) {
        this.userId = userId;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }

}
