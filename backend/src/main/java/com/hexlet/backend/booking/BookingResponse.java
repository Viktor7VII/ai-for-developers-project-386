package com.hexlet.backend.booking;

import java.time.Instant;

public record BookingResponse(
		Long id,
		Long eventTypeId,
		String eventTypeTitle,
		String guestName,
		String guestEmail,
		Instant startAt,
		Instant endAt,
		BookingStatus status,
		Instant createdAt
) {

	static BookingResponse from(Booking booking) {
		return new BookingResponse(
				booking.getId(),
				booking.getEventType().getId(),
				booking.getEventType().getTitle(),
				booking.getGuestName(),
				booking.getGuestEmail(),
				booking.getStartAt(),
				booking.getEndAt(),
				booking.getStatus(),
				booking.getCreatedAt()
		);
	}
}
