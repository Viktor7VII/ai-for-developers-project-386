package com.hexlet.backend.eventtype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating an event type.
 * The read-only {@code id} field from the contract is ignored if sent.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventTypeRequest(
		@NotBlank
		@Size(max = 100)
		@Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
				message = "must contain only lowercase letters, digits and single hyphens")
		String slug,

		@NotBlank
		@Size(max = 200)
		String title,

		@Size(max = 2000)
		String description,

		@NotNull
		@Min(5)
		@Max(720)
		Integer durationMinutes
) {
}
