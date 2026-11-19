package com.hexlet.backend.booking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import com.hexlet.backend.eventtype.EventType;
import com.hexlet.backend.schedule.Schedule;
import com.hexlet.backend.schedule.ScheduleService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlotService {

	private final ScheduleService scheduleService;
	private final BookingRepository bookingRepository;
	private final Clock clock;

	public SlotService(ScheduleService scheduleService, BookingRepository bookingRepository, Clock clock) {
		this.scheduleService = scheduleService;
		this.bookingRepository = bookingRepository;
		this.clock = clock;
	}

	/** Free slots of the event type that start within [from, to). */
	@Transactional(readOnly = true)
	public List<TimeSlot> findFreeSlots(EventType eventType, Instant from, Instant to) {
		Schedule schedule = scheduleService.getSchedule();
		Duration duration = Duration.ofMinutes(eventType.getDurationMinutes());
		// The owner is a single person: bookings of any event type make the time busy.
		List<TimeSlot> busy = bookingRepository.findOverlapping(from, to.plus(duration)).stream()
				.map(booking -> new TimeSlot(booking.getStartAt(), booking.getEndAt()))
				.toList();
		return SlotCalculator.calculate(
				ZoneId.of(schedule.getTimeZone()),
				schedule.getIntervals(),
				duration,
				from,
				to,
				clock.instant(),
				busy
		);
	}

	/** Whether a slot starting exactly at {@code startAt} is currently free. */
	@Transactional(readOnly = true)
	public boolean isFree(EventType eventType, Instant startAt) {
		return findFreeSlots(eventType, startAt, startAt.plusNanos(1)).stream()
				.anyMatch(slot -> slot.startAt().equals(startAt));
	}
}
