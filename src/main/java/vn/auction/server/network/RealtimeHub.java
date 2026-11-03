package vn.auction.server.network;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import vn.auction.common.dto.AuctionSnapshot;
import vn.auction.common.dto.RoomEvent;
import vn.auction.common.protocol.AuctionObserver;
import vn.auction.common.protocol.EventType;

/**
 * Quản lý các phòng đấu giá theo auctionId và phát thông điệp thời gian thực đến các client.
 * Phụ trách: TV2.
 */
public class RealtimeHub implements AuctionObserver {
    private final Map<UUID, Set<UUID>> roomSubscribers = new ConcurrentHashMap<>();

    public void joinRoom(UUID auctionId, UUID connectionId) {
        roomSubscribers.computeIfAbsent(auctionId, k -> ConcurrentHashMap.newKeySet()).add(connectionId);
    }

    public void leaveRoom(UUID auctionId, UUID connectionId) {
        Set<UUID> subscribers = roomSubscribers.get(auctionId);
        if (subscribers != null) {
            subscribers.remove(connectionId);
            if (subscribers.isEmpty()) {
                roomSubscribers.remove(auctionId);
            }
        }
    }

    public Set<UUID> getSubscribers(UUID auctionId) {
        return roomSubscribers.getOrDefault(auctionId, Collections.emptySet());
    }

    @Override
    public void onChanged(AuctionSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        RoomEvent event = new RoomEvent(
            EventType.STATE_CHANGED,
            snapshot.auctionId(),
            snapshot.version(),
            snapshot
        );
        broadcast(snapshot.auctionId(), event);
    }

    public void broadcast(UUID auctionId, RoomEvent event) {
        // Gửi tới writer queue của các connection trong phòng
    }
}

