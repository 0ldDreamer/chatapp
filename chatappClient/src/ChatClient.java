import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient {
    private String hostname;
    private int port;
    private String userName;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;
    private JButton loginButton;
    private JTextField loginField;
    private JPanel loginPanel;
    private JPanel chatPanel;

    public ChatClient() {
        this.hostname = "10.161.219.13"; // 修改为实际服务器地址
        this.port = 12345;
        setupUI(); // 设置用户界面
    }

    private void setupUI() {
        frame = new JFrame("匿名聊天室"); // 创建主窗口
        textArea = new JTextArea(); // 创建文本区域用于显示聊天记录
        textArea.setEditable(false); // 设置文本区域不可编辑
        textArea.setFont(new Font("宋体", Font.PLAIN, 14)); // 设置字体为宋体，大小为14
        textField = new JTextField(); // 创建文本框用于输入消息
        sendButton = new JButton("发送"); // 创建发送按钮
        sendButton.setBackground(Color.PINK); // 设置发送按钮的背景颜色为粉色
        loginButton = new JButton("开始聊天"); // 创建登录按钮
        loginButton.setBackground(Color.PINK); // 设置登录按钮的背景颜色为粉色
        loginField = new JTextField(20); // 创建用户名输入框，长度为20

        loginPanel = new JPanel(new BorderLayout()); // 创建登录面板，使用边界布局
        chatPanel = new JPanel(new BorderLayout()); // 创建聊天面板，使用边界布局

        loginPanel.add(new JLabel("取个名字吧:"), BorderLayout.WEST); // 登录面板左边添加标签
        loginPanel.add(loginField, BorderLayout.CENTER); // 登录面板中间添加用户名输入框
        loginPanel.add(loginButton, BorderLayout.EAST); // 登录面板右边添加登录按钮

        JScrollPane scrollPane = new JScrollPane(textArea); // 创建滚动面板
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI()); // 设置滚动条的UI为自定义UI
        chatPanel.add(scrollPane, BorderLayout.CENTER); // 聊天面板中间添加文本区域，并使用滚动条

        JPanel inputPanel = new JPanel(new BorderLayout()); // 创建输入面板，使用边界布局
        inputPanel.add(textField, BorderLayout.CENTER); // 输入面板中间添加文本框
        inputPanel.add(sendButton, BorderLayout.EAST); // 输入面板右边添加发送按钮
        chatPanel.add(inputPanel, BorderLayout.SOUTH); // 将输入面板添加到聊天面板南边

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置关闭操作
        frame.setSize(600, 450); // 设置窗口大小
        frame.add(loginPanel, BorderLayout.NORTH); // 主窗口北边添加登录面板
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true); // 显示窗口

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userName = loginField.getText(); // 获取输入的用户名
                if (userName != null && !userName.trim().isEmpty()) {
                    connectToServer(); // 连接到服务器
                } else {
                    JOptionPane.showMessageDialog(frame, "用户名不能为空", "错误", JOptionPane.ERROR_MESSAGE); // 显示错误消息
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage(); // 发送消息
            }
        });

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage(); // 在文本框中按回车键也发送消息
            }
        });
    }

    private void connectToServer() {
        try {
            socket = new Socket(hostname, port); // 连接到服务器

            InputStream input = socket.getInputStream(); // 获取输入流
            reader = new BufferedReader(new InputStreamReader(input)); // 创建BufferedReader读取消息

            OutputStream output = socket.getOutputStream(); // 获取输出流
            writer = new PrintWriter(output, true); // 创建PrintWriter发送消息

            new ReadThread().start(); // 启动读取消息的线程

            writer.println(userName); // 发送用户名到服务器
            frame.remove(loginPanel); // 移除登录面板
            frame.add(chatPanel, BorderLayout.CENTER); // 添加聊天面板
            frame.revalidate(); // 重新验证布局
            frame.repaint(); // 重绘界面

        } catch (UnknownHostException ex) {
            System.out.println("服务器未找到: " + ex.getMessage()); // 处理未知主机异常
        } catch (IOException ex) {
            System.out.println("I/O 错误: " + ex.getMessage()); // 处理IO异常
        }
    }

    private void sendMessage() {
        String text = textField.getText(); // 获取输入的消息
        writer.println(text); // 发送消息到服务器
        textField.setText(""); // 清空输入框
    }

    class ReadThread extends Thread {
        public void run() {
            String response;
            try {
                while ((response = reader.readLine()) != null) {
                    textArea.append(response + "\n"); // 读取并显示消息
                }
            } catch (IOException ex) {
                System.out.println("读取服务器消息错误: " + ex.getMessage()); // 处理读取消息时的IO异常
            }
        }
    }

    class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = Color.PINK; // 设置滑块颜色为粉色
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            JButton button = super.createDecreaseButton(orientation);
            button.setBackground(Color.PINK); // 设置向上的箭头颜色为粉色
            return button;
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            JButton button = super.createIncreaseButton(orientation);
            button.setBackground(Color.PINK); // 设置向下的箭头颜色为粉色
            return button;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ChatClient(); // 启动客户端
            }
        });
    }
}