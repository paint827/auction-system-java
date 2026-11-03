package vn.auction.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Điểm khởi chạy chính của Server đấu giá.
 */
public class ServerMain {
    private static final int DEFAULT_PORT = 5050;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String portProperty = System.getProperty("auction.port");
        if (portProperty != null) {
            try {
                port = Integer.parseInt(portProperty);
            } catch (NumberFormatException e) {
                System.err.println("Cổng không hợp lệ, sử dụng mặc định: " + DEFAULT_PORT);
            }
        }

        System.out.println("=== Khởi động Auction Server trên cổng " + port + " ===");

        ExecutorService handlers = Executors.newFixedThreadPool(16);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Đang tắt Server và giải phóng tài nguyên...");
            handlers.shutdownNow();
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server đã sẵn sàng lắng nghe kết nối.");
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Kết nối mới từ: " + clientSocket.getRemoteSocketAddress());
                // TODO: Bàn giao ClientHandler khi TV2 hoàn thiện socket handler
            }
        } catch (IOException e) {
            System.err.println("Lỗi ServerSocket: " + e.getMessage());
        } finally {
            handlers.shutdownNow();
        }
    }
}

