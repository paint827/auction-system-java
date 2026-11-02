package vn.auction.common.protocol;

/**
 * Danh sách command được hỗ trợ trong hệ thống đấu giá.
 */
public enum Command {
    REGISTER,
    LOGIN,
    LOGOUT,
    LIST_AUCTIONS,
    GET_AUCTION,
    JOIN_ROOM,
    LEAVE_ROOM,
    PLACE_BID,
    CREATE_AUCTION,
    CANCEL_AUCTION,
    CONFIRM_PAYMENT,
    LIST_MY_ITEMS,
    CREATE_ITEM,
    UPDATE_ITEM,
    DELETE_ITEM,
    LIST_USERS,
    BAN_USER,
    UNBAN_USER
}

