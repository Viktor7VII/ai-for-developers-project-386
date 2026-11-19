package com.hexlet.backend.booking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.hexlet.backend.common.BadRequestException;
import com.hexlet.backend.common.ConflictException;
import com.hexlet.backend.eventtype.EventType;
import com.hexlet.backend.eventtype.EventTypeService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class BookingService {

	static final Duration MAX_SLOTS_RANGE = Duration.ofDays(62);

	private final EventTypeService eventTypeService;
	private final SlotService slotService;
	private final BookingRepository bookingRepository;
	private final TransactionTemplate transactionTemplate;
	private final Clock clock;

	/**
	 * Serializes "check availability + insert" so two visitors can't book the same time.
	 * The lock is held until the transaction commits. This is enough for a single application
	 * instance; the unique constraint on start time is an additional database-level guard.
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

	@Transactional(readOnly = true)
	public List<TimeSlot> listFreeSlots(String slug, Instant from, Instant to) {
		if (!from.isBefore(to)) {
			throw new BadRequestException("from must be before to");
		}
		if (Duration.between(from, to).compareTo(MAX_SLOTS_RANGE) > 0) {
			throw new BadRequestException("range between from and to must not exceed "
					+ MAX_SLOTS_RANGE.toDays() + " days");
		}
		EventType eventType = eventTypeService.findBySlug(slug);
		return slotService.findFreeSlots(eventType, from, to);
	}

	public BookingResponse create(String slug, BookingRequest request) {
		bookingLock.lock();
		try {
			return transactionTemplate.execute(status -> createInTransaction(slug, request));
		} finally {
			bookingLock.unlock();
		}
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> listAll() {
		return bookingRepository.findAllWithEventTypeOrderByStartAt().stream()
				.map(BookingResponse::from)
				.toList();
	}

	private BookingResponse createInTransaction(String slug, BookingRequest request) {
		EventType eventType = eventTypeService.findBySlug(slug);
		Instant startAt = request.startAt();
		if (!slotService.isFree(eventType, startAt)) {
			throw new ConflictException("Selected time slot is not available");
		}
		Booking booking = new Booking(
				eventType,
				request.guestName().strip(),
				request.guestEmail().strip(),
				startAt,
				startAt.plus(Duration.ofMinutes(eventType.getDurationMinutes())),
				// Millisecond precision: same value in the response and in the database, and JS-friendly.
				clock.instant().truncatedTo(ChronoUnit.MILLIS)
		);
		return BookingResponse.from(bookingRepository.saveAndFlush(booking));
	}
}
