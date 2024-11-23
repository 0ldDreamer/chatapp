import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class ChatServer {
    private int port; // 服务器端口号
    private Set<String> userNames = new HashSet<>(); // 存储连接的用户名集合
    private Set<UserThread> userThreads = new HashSet<>(); // 存储连接的用户线程集合
    private Connection conn; // 数据库连接对象

    public ChatServer(int port) {
        this.port = port;
        connectToDatabase(); // 连接到数据库
    }

    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) { // 创建服务器套接字
            System.out.println("聊天服务器正在监听端口 " + port);

            while (true) {
                Socket socket = serverSocket.accept(); // 等待用户连接
                System.out.println("新用户连接");

                UserThread newUser = new UserThread(socket, this); // 为新连接的用户创建线程
                userThreads.add(newUser); // 将用户线程添加到集合中
                newUser.start(); // 启动用户线程
            }

        } catch (IOException ex) {
            System.out.println("服务器错误: " + ex.getMessage());
            ex.printStackTrace();
        }
    }



    void broadcast(String message, UserThread excludeUser) {
        for (UserThread aUser : userThreads) { // 遍历所有用户线程
            if (aUser != excludeUser) { // 排除发送消息的用户
                aUser.sendMessage(message); // 向其他用户发送消息
            } else {
                aUser.sendMessage("你: " + message); // 发送消息给发送者
            }
        }
        saveMessageToDatabase(message); // 将消息保存到数据库
    }

    void addUserName(String userName) {
        userNames.add(userName); // 添加用户名到集合中
    }

    void removeUser(String userName, UserThread aUser) {
        boolean removed = userNames.remove(userName); // 从集合中移除用户名
        if (removed) {
            userThreads.remove(aUser); // 从集合中移除用户线程
            System.out.println("用户 " + userName + " 已退出");
        }
    }

    private String getUserList() {
        StringBuilder userList = new StringBuilder("当前在线的用户: ");
        for (String userName : userNames) {
            userList.append(userName).append(" ");
        }

        return userList.toString();
    }

    private void connectToDatabase() {
        try {
            String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=chatapp;user=admin;password=admin"; // 修改为你的数据库连接信息
            conn = DriverManager.getConnection(connectionUrl); // 连接到数据库
            if (conn != null) {
                System.out.println("已连接到数据库。");
            }

        } catch (SQLException e) {
            System.out.println("数据库连接错误: " + e.getMessage());
        }
    }

    private void saveMessageToDatabase(String message) {
        String insertSQL = "INSERT INTO messages (message) VALUES (?)"; // 插入消息的SQL语句
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, message); // 设置消息内容
            pstmt.executeUpdate(); // 执行插入操作
        } catch (SQLException e) {
            System.out.println("保存消息错误: " + e.getMessage());
        }
    }

    private List<String> loadMessagesFromDatabase() {
        List<String> messages = new ArrayList<>(); // 存储消息的列表
        String querySQL = "SELECT message FROM messages"; // 查询消息的SQL语句
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(querySQL)) {
            while (rs.next()) {
                messages.add(rs.getString("message")); // 将查询到的消息添加到列表中
            }
        } catch (SQLException e) {
            System.out.println("加载消息错误: " + e.getMessage());
        }
        return messages; // 返回消息列表
    }

    //用于管理每个连接到服务器的用户。主要功能包括处理用户输入、发送和接收消息、加载历史消息以及广播用户状态变化（连接和退出）
    class UserThread extends Thread {
        private Socket socket; // 用户套接字
        private ChatServer server; // 服务器实例
        private PrintWriter writer; // 输出流，用于发送消息

        public UserThread(Socket socket, ChatServer server) {
            this.socket = socket;
            this.server = server;
        }

        public void run() {
            try {
                InputStream input = socket.getInputStream(); // 获取输入流
                BufferedReader reader = new BufferedReader(new InputStreamReader(input)); // 创建缓冲读取器

                OutputStream output = socket.getOutputStream(); // 获取输出流
                writer = new PrintWriter(output, true); // 创建打印写入器


                // 从数据库加载以前的消息
                List<String> messages = server.loadMessagesFromDatabase();
                for (String message : messages) {
                    writer.println(message); // 向用户显示以前的消息
                }

                String userName = reader.readLine(); // 读取用户名
                server.addUserName(userName); // 添加用户名到集合中

                //广播欢迎消息和当前在线用户
                StringBuilder serverMessage_sb = new StringBuilder();
                serverMessage_sb.append("-----欢迎用户 " + userName + " 进入聊天室 =w= ");
                serverMessage_sb.append(getUserList() + "-----");
                String serverMessage = serverMessage_sb.toString();

                server.broadcast(serverMessage, this); // 广播新用户连接消息

                String clientMessage;

                try {
                    do {
                        clientMessage = reader.readLine(); // 读取用户消息
                        if (clientMessage != null) {
                            serverMessage = "[" + userName + "]: " + clientMessage;
                            server.broadcast(serverMessage, this); // 广播用户消息
                        }
                    } while (clientMessage != null && !clientMessage.equals("exit"));
                } catch (IOException ex) {
                    System.out.println("用户 " + userName + " 连接丢失: " + ex.getMessage());
                }

                server.removeUser(userName, this); // 移除用户
                socket.close(); // 关闭套接字

                serverMessage = "用户 " + userName + " 离开了聊天室";
                server.broadcast(serverMessage, this); // 广播用户退出消息

            } catch (IOException ex) {
                System.out.println("用户线程错误: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        void sendMessage(String message) {
            writer.println(message); // 发送消息给用户
        }
    }
    public static void main(String[] args) {
        int port = 12345; // 设置服务器端口号
        ChatServer server = new ChatServer(port);
        server.execute(); // 启动服务器
    }
}
