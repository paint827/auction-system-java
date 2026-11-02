package client.network;

import java.io.IOException;

public final class NetworkClient {

    private static final NetworkClient INSTANCE = new NetworkClient();

    private NetworkClient() { }

    public static NetworkClient getInstance() { return INSTANCE; }

    public void connect(String host, int port) throws IOException {
        // TODO STT5: mở Socket, tạo luồng gửi/nhận theo protocol của TV2
    }

    public void disconnect() {
        // TODO STT5: đóng socket
    }
}