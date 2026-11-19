package com.hexlet.backend.booking;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookingRequest(
		@NotNull
		Instant startAt,

		@NotBlank
		@Size(max = 200)
		String guestName,

		@NotBlank
		@Size(min = 3, max = 320)
		@Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "must be a valid email address")
		String guestEmail
) {
}
