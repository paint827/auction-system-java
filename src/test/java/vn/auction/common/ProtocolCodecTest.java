package vn.auction.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.auction.common.dto.AuctionSnapshot;
import vn.auction.common.dto.BidPayload;
import vn.auction.common.dto.Request;
import vn.auction.common.dto.Response;
import vn.auction.common.dto.RoomEvent;
import vn.auction.common.protocol.Command;
import vn.auction.common.protocol.ErrorCode;
import vn.auction.common.protocol.EventType;

class ProtocolCodecTest {

    @SuppressWarnings("unchecked")
    private <T> T roundTrip(T input) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(input);
            out.flush();
        }

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        try (ObjectInputStream in = new ObjectInputStream(byteIn)) {
            return (T) in.readObject();
        }
    }

    @Test
    @DisplayName("Serialization Round-trip cho Request và BidPayload")
    void testRequestCodec() throws Exception {
        UUID reqId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        BidPayload payload = new BidPayload(auctionId, 500000L);
        Request request = new Request(reqId, Command.PLACE_BID, payload);

        Request deserialized = roundTrip(request);

        Assertions.assertEquals(reqId, deserialized.requestId());
        Assertions.assertEquals(Command.PLACE_BID, deserialized.command());
        Assertions.assertInstanceOf(BidPayload.class, deserialized.payload());

        BidPayload p = (BidPayload) deserialized.payload();
        Assertions.assertEquals(auctionId, p.auctionId());
        Assertions.assertEquals(500000L, p.amount());
    }

    @Test
    @DisplayName("Serialization Round-trip cho Response thành công và lỗi")
    void testResponseCodec() throws Exception {
        UUID reqId = UUID.randomUUID();
        Response okResponse = Response.ok(reqId, "SUCCESS_DATA");
        Response deserOk = roundTrip(okResponse);

        Assertions.assertTrue(deserOk.success());
        Assertions.assertEquals(reqId, deserOk.requestId());
        Assertions.assertEquals("SUCCESS_DATA", deserOk.data());

        Response errResponse = Response.error(reqId, ErrorCode.INVALID_BID, "Giá đặt thấp hơn bước giá tối thiểu");
        Response deserErr = roundTrip(errResponse);

        Assertions.assertFalse(deserErr.success());
        Assertions.assertEquals(reqId, deserErr.requestId());
        Assertions.assertEquals(ErrorCode.INVALID_BID, deserErr.errorCode());
    }

    @Test
    @DisplayName("Serialization Round-trip cho RoomEvent và AuctionSnapshot")
    void testRoomEventCodec() throws Exception {
        UUID auctionId = UUID.randomUUID();
        UUID winnerId = UUID.randomUUID();
        Instant now = Instant.now();
        AuctionSnapshot snapshot = new AuctionSnapshot(
            auctionId, "RUNNING", 150000L, winnerId, now.plusSeconds(60), now, 60L, 1L
        );
        RoomEvent event = new RoomEvent(EventType.BID_PLACED, auctionId, 1L, snapshot);

        RoomEvent deserEvent = roundTrip(event);

        Assertions.assertEquals(EventType.BID_PLACED, deserEvent.type());
        Assertions.assertEquals(auctionId, deserEvent.auctionId());
        Assertions.assertEquals(1L, deserEvent.version());
        Assertions.assertNotNull(deserEvent.snapshot());
        Assertions.assertEquals(150000L, deserEvent.snapshot().price());
    }
}

