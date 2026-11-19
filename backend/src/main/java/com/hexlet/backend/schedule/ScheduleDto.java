package com.hexlet.backend.schedule;

import java.time.DayOfWeek;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request and response body of the schedule endpoints, matching the {@code Schedule} contract model.
 */
public record ScheduleDto(
		@NotBlank
		@Size(max = 64)
		String timeZone,

		@NotNull
		@Size(max = 100)
		List<@NotNull @Valid IntervalDto> intervals
) {

	static final String TIME_PATTERN = "^([01][0-9]|2[0-3]):[0-5][0-9]$";

	public record IntervalDto(
			@NotNull
			DayOfWeek dayOfWeek,

			@NotNull
			@Pattern(regexp = TIME_PATTERN, message = "must be in HH:mm format")
			String startTime,

			@NotNull
			@Pattern(regexp = TIME_PATTERN, message = "must be in HH:mm format")
			String endTime
	) {
	}
}
