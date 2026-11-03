package vn.auction.server.dao;

import java.io.Serializable;
import java.util.List;

/**
 * Bản chụp dữ liệu nghiệp vụ của toàn bộ hệ thống để lưu trữ bền vững (Persistence).
 * Phụ trách: TV2.
 */
public record SystemSnapshot(
    int schemaVersion,
    long timestamp,
    List<Serializable> users,
    List<Serializable> items,
    List<Serializable> auctions,
    List<Serializable> bids
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

