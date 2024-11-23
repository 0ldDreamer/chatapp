import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ClearMessage {
    // JDBC驱动和数据库URL
    static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=chatapp";

    // 数据库用户和密码
    static final String USER = "admin";
    static final String PASS = "admin";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册JDBC驱动
            Class.forName(JDBC_DRIVER);

            // 打开连接
            System.out.println("连接数据库中...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            System.out.println("清空表messages中的记录中...");
            stmt = conn.createStatement();
            String sql = "TRUNCATE TABLE messages";
            stmt.executeUpdate(sql);
            System.out.println("成功清空表messages中的记录！");

        } catch (SQLException se) {
            // 处理JDBC错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理Class.forName错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            } // 什么都不做
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
