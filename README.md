# 基于 TCP/IP 和 Socket 协议的即时聊天程序



## 介绍

1. 这是我在《计算机网络》课程的大作业-----**在线聊天程序**

2. 本程序是一个简单的基于 **TCP/IP** 协议的 **客户端-服务器** 聊天程序，客户端与服务器之间通过 **Socket** 进行即时消息交换。客户端通过 **Socket** 连接到服务器，服务器通过 **ServerSocket** 监听客户端的连接并处理消息。该程序使用 **Java** 编写，并依赖于 **SQL Server** 数据库来存储聊天记录。

## 注意

本聊天程序使用 **SQL Server** 数据库，若要使用 MySQL 数据库，请自行引入 MySQL JDBC 驱动：

1. 将 MySQL JDBC 驱动 JAR 包放在 `chatappServer` 的 `lib` 文件夹中。
2. 在 **Project Structure** 中进行配置：
   - 选择 **Modules** -> **Dependencies** -> 点击下方的 `+` 号 -> 选择 **Library** -> 添加刚才的驱动。



## 使用前必要的步骤

1. 运行 SQL 文件，创建数据库。

2. **chatappServer**中：
   - 将 `ChatappServer.java` 第 72 行的内容修改为你的数据库连接信息。
   - 将 `ClearMessage.java` 第 9 行的内容修改为你的数据库连接信息。

3. **chatappClient**中：
   - 将 `ChatClient.class` 第 25 行的内容修改为你当前的 IP 地址。



## 使用方法

使用 IntelliJ IDEA 打开一个 `chatappServer`，运行`ChatappServer.java`，然后打开两个或多个 `chatappClient`（可复制多个chatappClient），运行`ChatClient.class`，即可进行聊天。
运行 `chatappServer` 的 `ClearMessage.java` 清空聊天记录。