/**
 * Động cơ lõi (Core Engine) xử lý logic nghiệp vụ và đồng thời phía Server:
 * - Quản lý khóa đa luồng (ReentrantLock)
 * - Máy trạng thái phiên đấu giá (State Machine)
 * - Cơ chế chống phá giá phút chót (Anti-sniping)
 * - Cơ chế đấu giá tự động (Auto-bidding)
 *
 * Phụ trách chính: TV1.
 */
package com.auction.server.core;

