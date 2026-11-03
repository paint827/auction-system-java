package vn.auction.common.exception;

/**
 * Ngoại lệ cơ sở cho các lỗi nghiệp vụ trong hệ thống đấu giá.
 */
public class AuctionException extends Exception {
    private final String errorCode;

    public AuctionException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuctionException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

