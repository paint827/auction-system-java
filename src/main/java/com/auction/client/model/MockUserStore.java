package com.auction.client.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý danh sách tài khoản tạm thời phía Client khi chưa kết nối Socket Server.
 * Giúp đảm bảo dữ liệu đăng ký không bị mất trong quá trình chạy và phân quyền đúng quy định.
 */
public final class MockUserStore {

    public record UserInfo(String username, String password, String role) {}

    private static final Map<String, UserInfo> USERS = new ConcurrentHashMap<>();

    static {
        // Chỉ có admin và seller là có mật khẩu mặc định là 123
        USERS.put("admin", new UserInfo("admin", "123", "ADMIN"));
        USERS.put("seller", new UserInfo("seller", "123", "SELLER"));
    }

    private MockUserStore() { }

    /**
     * Kiểm tra xem tên đăng nhập đã được đăng ký chưa (không phân biệt hoa thường).
     */
    public static boolean exists(String username) {
        if (username == null) return false;
        return USERS.containsKey(username.trim().toLowerCase());
    }

    /**
     * Đăng ký tài khoản mới vào bộ nhớ.
     */
    public static boolean register(String username, String password, String role) {
        if (username == null || password == null || exists(username)) {
            return false;
        }
        String cleanUsername = username.trim();
        USERS.put(cleanUsername.toLowerCase(), new UserInfo(cleanUsername, password.trim(), role));
        return true;
    }

    /**
     * Xác thực thông tin đăng nhập:
     * - Trả về UserInfo nếu đúng cả username và password
     * - Trả về null nếu sai username hoặc sai password
     */
    public static UserInfo authenticate(String username, String password) {
        if (username == null || password == null) return null;
        UserInfo user = USERS.get(username.trim().toLowerCase());
        if (user != null && user.password().equals(password.trim())) {
            return user;
        }
        return null;
    }
}
