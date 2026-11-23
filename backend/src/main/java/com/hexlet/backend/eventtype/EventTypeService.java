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
	public List<EventType> list() {
		return repository.findAllByOrderByIdAsc();
	}

	@Transactional(readOnly = true)
	public EventType get(long id) {
		return findById(id);
	}

	@Transactional(readOnly = true)
	public EventType findBySlug(String slug) {
		return repository.findBySlug(slug)
				.orElseThrow(() -> new NotFoundException("Event type '" + slug + "' not found"));
	}

	@Transactional
	public EventType create(String slug, String title, String description, int durationMinutes) {
		if (repository.existsBySlug(slug)) {
			throw slugTaken(slug);
		}
		EventType eventType = new EventType(slug, title.strip(), normalizeDescription(description), durationMinutes);
		return repository.saveAndFlush(eventType);
	}

	@Transactional
	public EventType update(long id, String slug, String title, String description, int durationMinutes) {
		EventType eventType = findById(id);
		if (repository.existsBySlugAndIdNot(slug, id)) {
			throw slugTaken(slug);
		}
		eventType.update(slug, title.strip(), normalizeDescription(description), durationMinutes);
		return repository.saveAndFlush(eventType);
	}

	/**
	 * Deletes an event type that no booking references. A booking created concurrently is still
	 * caught by the foreign key and reported as a conflict (see ApiExceptionHandler).
	 */
	@Transactional
	public void delete(long id) {
		EventType eventType = findById(id);
		if (repository.hasBookings(id)) {
			throw new ConflictException("Event type " + id + " has bookings and cannot be deleted");
		}
		repository.delete(eventType);
		repository.flush();
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
