import jakarta.servlet.ServletConfig;
import java.sql.Connection;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Random;

public class dbconnector {
    Random rand = new Random();
    private DataSource dataSource;



    public Connection getReadConnection() throws SQLException {
        int randint = rand.nextInt(1);
        if(randint == 0){
            try {
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/writeconnect");
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/readconnect");
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
        return dataSource.getConnection();
    }

    public Connection getWriteConnection() throws SQLException {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/writeconnect");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return dataSource.getConnection();
    }
}



