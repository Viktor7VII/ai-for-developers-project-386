package com.hexlet.backend.booking;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.hexlet.backend.common.ConflictException;
import com.hexlet.backend.eventtype.EventType;
import com.hexlet.backend.eventtype.EventTypeRepository;
import com.hexlet.backend.schedule.Schedule;
import com.hexlet.backend.schedule.ScheduleRepository;
import com.hexlet.backend.schedule.WeeklyInterval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingFlowTests {

	/** Sunday 2026-11-15 12:00 UTC; the next day is Monday 2026-11-16. */
	static final Instant NOW = Instant.parse("2026-11-15T12:00:00Z");

	private static final String MONDAY_FROM = "2026-11-16T00:00:00Z";
	private static final String MONDAY_TO = "2026-11-17T00:00:00Z";

	@TestConfiguration
	static class FixedClockConfig {
		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(NOW, ZoneOffset.UTC);
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private EventTypeRepository eventTypeRepository;

	@Autowired
	private ScheduleRepository scheduleRepository;

	@BeforeEach
	void setUp() {
		bookingRepository.deleteAll();
		eventTypeRepository.deleteAll();
		scheduleRepository.deleteAll();
		// Monday 09:00-11:00 UTC.
		scheduleRepository.save(new Schedule(Schedule.OWNER_SCHEDULE_ID, "UTC", List.of(
				new WeeklyInterval(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)))));
		eventTypeRepository.save(new EventType("consultation", "Консультация", "Обсудим проект", 30));
		eventTypeRepository.save(new EventType("strategy", "Стратегическая сессия", null, 60));
	}

	@Test
	void returnsPublicEventTypeBySlug() throws Exception {
		mockMvc.perform(get("/api/public/event-types/consultation"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("consultation"))
				.andExpect(jsonPath("$.title").value("Консультация"))
				.andExpect(jsonPath("$.durationMinutes").value(30));
	}

	@Test
	void returnsNotFoundForUnknownSlug() throws Exception {
		mockMvc.perform(get("/api/public/event-types/unknown"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/public/event-types/unknown/slots")
						.param("from", MONDAY_FROM).param("to", MONDAY_TO))
				.andExpect(status().isNotFound());
		book("unknown", "2026-11-16T09:00:00Z")
				.andExpect(status().isNotFound());
	}

	@Test
	void listsFreeSlots() throws Exception {
		slots("consultation")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)))
				.andExpect(jsonPath("$[0].startAt").value("2026-11-16T09:00:00Z"))
				.andExpect(jsonPath("$[0].endAt").value("2026-11-16T09:30:00Z"))
				.andExpect(jsonPath("$[3].startAt").value("2026-11-16T10:30:00Z"));
	}

	@Test
	void acceptsJavaScriptStyleDatesWithMilliseconds() throws Exception {
		mockMvc.perform(get("/api/public/event-types/consultation/slots")
						.param("from", "2026-11-16T00:00:00.000Z")
						.param("to", "2026-11-17T00:00:00.000Z"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)));
	}

	@Test
	void createsBookingAndRemovesSlot() throws Exception {
		book("consultation", "2026-11-16T09:30:00Z")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.eventTypeTitle").value("Консультация"))
				.andExpect(jsonPath("$.guestName").value("Иван Петров"))
				.andExpect(jsonPath("$.guestEmail").value("ivan@example.com"))
				.andExpect(jsonPath("$.startAt").value("2026-11-16T09:30:00Z"))
				.andExpect(jsonPath("$.endAt").value("2026-11-16T10:00:00Z"))
				.andExpect(jsonPath("$.status").value("CONFIRMED"))
				.andExpect(jsonPath("$.createdAt").value("2026-11-15T12:00:00Z"));

		slots("consultation")
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[0].startAt").value("2026-11-16T09:00:00Z"))
				.andExpect(jsonPath("$[1].startAt").value("2026-11-16T10:00:00Z"));
	}

	@Test
	void rejectsAlreadyBookedSlot() throws Exception {
		book("consultation", "2026-11-16T09:00:00Z").andExpect(status().isCreated());

		book("consultation", "2026-11-16T09:00:00Z")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(containsString("not available")));
	}

	@Test
	void bookingOfOneEventTypeBlocksOverlappingSlotsOfAnother() throws Exception {
		book("strategy", "2026-11-16T09:00:00Z").andExpect(status().isCreated());

		slots("consultation")
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].startAt").value("2026-11-16T10:00:00Z"));
		book("consultation", "2026-11-16T09:30:00Z").andExpect(status().isConflict());
	}

	@Test
	void rejectsSlotOutsideSchedule() throws Exception {
		// Tuesday is not in the schedule.
		book("consultation", "2026-11-17T09:00:00Z").andExpect(status().isConflict());
		// Not aligned to the slot grid.
		book("consultation", "2026-11-16T09:10:00Z").andExpect(status().isConflict());
		// 10:30 + 60 minutes does not fit into 09:00-11:00.
		book("strategy", "2026-11-16T10:30:00Z").andExpect(status().isConflict());
	}

	@Test
	void rejectsSlotInThePast() throws Exception {
		// Monday a week before NOW.
		book("consultation", "2026-11-09T09:00:00Z").andExpect(status().isConflict());
	}

	@Test
	void validatesGuestData() throws Exception {
		mockMvc.perform(post("/api/public/event-types/consultation/bookings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"startAt": "2026-11-16T09:00:00Z", "guestName": " ", "guestEmail": "not-an-email"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(containsString("guestName")))
				.andExpect(jsonPath("$.message").value(containsString("guestEmail")));

		mockMvc.perform(post("/api/public/event-types/consultation/bookings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"guestName": "Иван", "guestEmail": "ivan@example.com"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(containsString("startAt")));
	}

	@Test
	void validatesSlotsRange() throws Exception {
		mockMvc.perform(get("/api/public/event-types/consultation/slots").param("from", MONDAY_FROM))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(containsString("'to'")));

		mockMvc.perform(get("/api/public/event-types/consultation/slots")
						.param("from", MONDAY_TO).param("to", MONDAY_FROM))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(containsString("before")));

		mockMvc.perform(get("/api/public/event-types/consultation/slots")
						.param("from", "2026-11-16T00:00:00Z").param("to", "2027-01-31T00:00:00Z"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(containsString("62 days")));

		mockMvc.perform(get("/api/public/event-types/consultation/slots")
						.param("from", "yesterday").param("to", MONDAY_TO))
				.andExpect(status().isBadRequest());
	}

	@Test
	void ownerSeesBookingsOrderedByStartTime() throws Exception {
		book("strategy", "2026-11-16T10:00:00Z").andExpect(status().isCreated());
		book("consultation", "2026-11-16T09:00:00Z").andExpect(status().isCreated());

		mockMvc.perform(get("/api/bookings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].eventTypeTitle").value("Консультация"))
				.andExpect(jsonPath("$[0].startAt").value("2026-11-16T09:00:00Z"))
				.andExpect(jsonPath("$[1].eventTypeTitle").value("Стратегическая сессия"))
				.andExpect(jsonPath("$[1].endAt").value("2026-11-16T11:00:00Z"));
	}

	@Test
	void scheduleChangeDoesNotAffectExistingBookings() throws Exception {
		book("consultation", "2026-11-16T09:00:00Z").andExpect(status().isCreated());

		mockMvc.perform(put("/api/schedule")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"timeZone": "UTC", "intervals": [
								  {"dayOfWeek": "TUESDAY", "startTime": "09:00", "endTime": "10:00"}
								]}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/bookings"))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].startAt").value("2026-11-16T09:00:00Z"))
				.andExpect(jsonPath("$[0].status").value("CONFIRMED"));
		slots("consultation").andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void concurrentRequestsCannotBookSameSlot() throws Exception {
		int threads = 8;
		BookingRequest request = new BookingRequest(
				Instant.parse("2026-11-16T09:00:00Z"), "Гость", "guest@example.com");
		List<Callable<Boolean>> tasks = new ArrayList<>();
		for (int i = 0; i < threads; i++) {
			tasks.add(() -> {
				try {
					bookingService.create("consultation", request);
					return true;
				} catch (ConflictException e) {
					return false;
				}
			});
		}

		ExecutorService executor = Executors.newFixedThreadPool(threads);
		int successes = 0;
		try {
			for (Future<Boolean> result : executor.invokeAll(tasks)) {
				if (result.get()) {
					successes++;
				}
			}
		} finally {
			executor.shutdownNow();
		}

		assertEquals(1, successes);
		assertEquals(1, bookingRepository.count());
	}

	private ResultActions slots(String slug) throws Exception {
		return mockMvc.perform(get("/api/public/event-types/{slug}/slots", slug)
				.param("from", MONDAY_FROM)
				.param("to", MONDAY_TO));
	}

	private ResultActions book(String slug, String startAt) throws Exception {
		return mockMvc.perform(post("/api/public/event-types/{slug}/bookings", slug)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"startAt": "%s", "guestName": "Иван Петров", "guestEmail": "ivan@example.com"}
						""".formatted(startAt)));
	}
}
