package com.hexlet.backend.schedule;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import com.hexlet.backend.common.BadRequestException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

	static final String DEFAULT_TIME_ZONE = "UTC";

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private static final Comparator<WeeklyInterval> INTERVAL_ORDER = Comparator
			.comparing(WeeklyInterval::getDayOfWeek)
			.thenComparing(WeeklyInterval::getStartTime);

	private final ScheduleRepository repository;

	public ScheduleService(ScheduleRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public ScheduleDto get() {
		return toDto(getSchedule());
	}

	/**
	 * Returns the saved owner schedule or the default one (Mon-Fri 09:00-17:00) if nothing was saved yet.
	 * The default schedule is not persisted.
	 */
	@Transactional(readOnly = true)
	public Schedule getSchedule() {
		return repository.findById(Schedule.OWNER_SCHEDULE_ID).orElseGet(ScheduleService::defaultSchedule);
	}

	@Transactional
	public ScheduleDto update(ScheduleDto request) {
		String timeZone = parseTimeZone(request.timeZone());
		List<WeeklyInterval> intervals = request.intervals().stream()
				.map(ScheduleService::toInterval)
				.sorted(INTERVAL_ORDER)
				.toList();
		ensureNoOverlaps(intervals);

		Schedule schedule = repository.findById(Schedule.OWNER_SCHEDULE_ID)
				.orElseGet(() -> new Schedule(Schedule.OWNER_SCHEDULE_ID, timeZone, List.of()));
		schedule.replace(timeZone, intervals);
		return toDto(repository.saveAndFlush(schedule));
	}

	static Schedule defaultSchedule() {
		LocalTime start = LocalTime.of(9, 0);
		LocalTime end = LocalTime.of(17, 0);
		List<WeeklyInterval> intervals = List.of(
						DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
						DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
				.stream()
				.map(day -> new WeeklyInterval(day, start, end))
				.toList();
		return new Schedule(Schedule.OWNER_SCHEDULE_ID, DEFAULT_TIME_ZONE, intervals);
	}

	private static String parseTimeZone(String timeZone) {
		try {
			return ZoneId.of(timeZone.strip()).getId();
		} catch (DateTimeException e) {
			throw new BadRequestException("timeZone: unknown time zone '" + timeZone + "'");
		}
	}

	private static WeeklyInterval toInterval(ScheduleDto.IntervalDto dto) {
		LocalTime start = LocalTime.parse(dto.startTime(), TIME_FORMAT);
		LocalTime end = LocalTime.parse(dto.endTime(), TIME_FORMAT);
		if (!start.isBefore(end)) {
			throw new BadRequestException(
					"intervals: " + dto.dayOfWeek() + " " + dto.startTime() + "-" + dto.endTime()
							+ " has endTime that is not after startTime");
		}
		return new WeeklyInterval(dto.dayOfWeek(), start, end);
	}

	/** Expects intervals sorted by day and start time. Adjacent intervals (end == next start) are allowed. */
	private static void ensureNoOverlaps(List<WeeklyInterval> intervals) {
		for (int i = 1; i < intervals.size(); i++) {
			WeeklyInterval previous = intervals.get(i - 1);
			WeeklyInterval current = intervals.get(i);
			if (previous.getDayOfWeek() == current.getDayOfWeek()
					&& current.getStartTime().isBefore(previous.getEndTime())) {
				throw new BadRequestException(
						"intervals: " + current.getDayOfWeek() + " intervals "
								+ format(previous) + " and " + format(current) + " overlap");
			}
		}
	}

	private static ScheduleDto toDto(Schedule schedule) {
		List<ScheduleDto.IntervalDto> intervals = schedule.getIntervals().stream()
				.sorted(INTERVAL_ORDER)
				.map(interval -> new ScheduleDto.IntervalDto(
						interval.getDayOfWeek(),
						interval.getStartTime().format(TIME_FORMAT),
						interval.getEndTime().format(TIME_FORMAT)))
				.toList();
		return new ScheduleDto(schedule.getTimeZone(), intervals);
	}

	private static String format(WeeklyInterval interval) {
		return interval.getStartTime().format(TIME_FORMAT) + "-" + interval.getEndTime().format(TIME_FORMAT);
	}
}
