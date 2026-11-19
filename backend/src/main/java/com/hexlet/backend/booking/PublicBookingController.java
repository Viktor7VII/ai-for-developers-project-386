package com.hexlet.backend.booking;

import java.time.Instant;
import java.util.List;

import com.hexlet.backend.eventtype.EventTypeResponse;
import com.hexlet.backend.eventtype.EventTypeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for visitors. No registration is required.
 */
@RestController
@RequestMapping("/api/public/event-types/{slug}")
public class PublicBookingController {

	private final EventTypeService eventTypeService;
	private final BookingService bookingService;

	public PublicBookingController(EventTypeService eventTypeService, BookingService bookingService) {
		this.eventTypeService = eventTypeService;
		this.bookingService = bookingService;
	}

	@GetMapping
	public EventTypeResponse getEventType(@PathVariable String slug) {
		return eventTypeService.getBySlug(slug);
	}

	@GetMapping("/slots")
	public List<TimeSlot> listSlots(
			@PathVariable String slug,
			@RequestParam Instant from,
			@RequestParam Instant to
	) {
		return bookingService.listFreeSlots(slug, from, to);
	}

	@PostMapping("/bookings")
	@ResponseStatus(HttpStatus.CREATED)
	public BookingResponse createBooking(@PathVariable String slug, @Valid @RequestBody BookingRequest request) {
		return bookingService.create(slug, request);
	}
}
