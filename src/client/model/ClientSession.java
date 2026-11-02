package client.model;

public final class ClientSession {

    private static final ClientSession INSTANCE = new ClientSession();

    private String username;
    private String role; // BIDDER / SELLER / ADMIN (sau này đổi sang enum của TV4)

    private ClientSession() { }

    public static ClientSession getInstance() { return INSTANCE; }

    public void login(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public void logout() {
        this.username = null;
        this.role = null;
    }

    public boolean isLoggedIn() { return username != null; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}