package com.hexlet.backend.eventtype;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventTypeResponse(
		Long id,
		String slug,
		String title,
		String description,
		int durationMinutes
) {

	static EventTypeResponse from(EventType eventType) {
		return new EventTypeResponse(
				eventType.getId(),
				eventType.getSlug(),
				eventType.getTitle(),
				eventType.getDescription(),
				eventType.getDurationMinutes()
		);
	}
}
