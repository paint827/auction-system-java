package vn.auction.common.protocol;

/**
 * Mã lỗi chuẩn hoá trả về cho Client.
 */
public final class ErrorCode {
    public static final String INVALID_BID = "INVALID_BID";
    public static final String AUCTION_CLOSED = "AUCTION_CLOSED";
    public static final String UNAUTHENTICATED = "UNAUTHENTICATED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCode() {
        // Utility class
    }
}

