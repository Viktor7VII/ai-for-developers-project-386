package com.hexlet.backend.booking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.hexlet.backend.common.ConflictException;
import com.hexlet.backend.eventtype.EventType;
import com.hexlet.backend.eventtype.EventTypeService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class BookingService {

	private final EventTypeService eventTypeService;
	private final SlotService slotService;
	private final BookingRepository bookingRepository;
	private final TransactionTemplate transactionTemplate;
	private final Clock clock;

	/**
	 * Serializes "check availability + insert" so two guests can't book overlapping time (ADR-0002).
	 * The lock is held until the transaction commits; the unique start constraint is a database-level guard.
	 */
	private final ReentrantLock bookingLock = new ReentrantLock();

	public BookingService(
			EventTypeService eventTypeService,
			SlotService slotService,
			BookingRepository bookingRepository,
			PlatformTransactionManager transactionManager,
			Clock clock
	) {
		this.eventTypeService = eventTypeService;
		this.slotService = slotService;
		this.bookingRepository = bookingRepository;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.clock = clock;
	}

	/** Free slots of the event type within the booking window. */
	@Transactional(readOnly = true)
	public List<TimeSlot> listFreeSlots(String slug) {
		return slotService.findFreeSlots(eventTypeService.findBySlug(slug));
	}

	public Booking create(String slug, Instant startAt, String guestName, String guestEmail) {
		bookingLock.lock();
		try {
			return transactionTemplate.execute(status -> createInTransaction(slug, startAt, guestName, guestEmail));
		} finally {
			bookingLock.unlock();
		}
	}

	@Transactional(readOnly = true)
	public List<Booking> listAll() {
		return bookingRepository.findAllWithEventTypeOrderByStartAt();
	}

	private Booking createInTransaction(String slug, Instant startAt, String guestName, String guestEmail) {
		EventType eventType = eventTypeService.findBySlug(slug);
		// Re-checks the booking window, slot grid, schedule and overlaps with all bookings.
		if (!slotService.isFree(eventType, startAt)) {
			throw new ConflictException("Selected time slot is not available");
		}
		Booking booking = new Booking(
				eventType,
				guestName.strip(),
				guestEmail.strip(),
				startAt,
				startAt.plus(Duration.ofMinutes(eventType.getDurationMinutes())),
				// Millisecond precision: same value in the response and in the database, and JS-friendly.
				clock.instant().truncatedTo(ChronoUnit.MILLIS)
		);
		return bookingRepository.saveAndFlush(booking);
	}
}
