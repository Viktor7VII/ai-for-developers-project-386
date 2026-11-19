package com.hexlet.backend.eventtype;

import java.util.List;

import com.hexlet.backend.common.ConflictException;
import com.hexlet.backend.common.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventTypeService {

	private final EventTypeRepository repository;

	public EventTypeService(EventTypeRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public List<EventTypeResponse> list() {
		return repository.findAllByOrderByIdAsc().stream()
				.map(EventTypeResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public EventTypeResponse get(long id) {
		return EventTypeResponse.from(findById(id));
	}

	@Transactional(readOnly = true)
	public EventTypeResponse getBySlug(String slug) {
		return EventTypeResponse.from(findBySlug(slug));
	}

	@Transactional(readOnly = true)
	public EventType findBySlug(String slug) {
		return repository.findBySlug(slug)
				.orElseThrow(() -> new NotFoundException("Event type '" + slug + "' not found"));
	}

	@Transactional
	public EventTypeResponse create(EventTypeRequest request) {
		if (repository.existsBySlug(request.slug())) {
			throw slugTaken(request.slug());
		}
		EventType eventType = new EventType(
				request.slug(),
				request.title().strip(),
				normalizeDescription(request.description()),
				request.durationMinutes()
		);
		return EventTypeResponse.from(repository.saveAndFlush(eventType));
	}

	@Transactional
	public EventTypeResponse update(long id, EventTypeRequest request) {
		EventType eventType = findById(id);
		if (repository.existsBySlugAndIdNot(request.slug(), id)) {
			throw slugTaken(request.slug());
		}
		eventType.update(
				request.slug(),
				request.title().strip(),
				normalizeDescription(request.description()),
				request.durationMinutes()
		);
		return EventTypeResponse.from(repository.saveAndFlush(eventType));
	}

	private EventType findById(long id) {
		return repository.findById(id)
				.orElseThrow(() -> new NotFoundException("Event type " + id + " not found"));
	}

	private static ConflictException slugTaken(String slug) {
		return new ConflictException("Event type with slug '" + slug + "' already exists");
	}

	private static String normalizeDescription(String description) {
		if (description == null || description.isBlank()) {
			return null;
		}
		return description.strip();
	}
}
