package com.hexlet.backend.eventtype;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-types")
public class EventTypeController {

	private final EventTypeService service;

	public EventTypeController(EventTypeService service) {
		this.service = service;
	}

	@GetMapping
	public List<EventTypeResponse> list() {
		return service.list();
	}

	@GetMapping("/{id}")
	public EventTypeResponse get(@PathVariable long id) {
		return service.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EventTypeResponse create(@Valid @RequestBody EventTypeRequest request) {
		return service.create(request);
	}

	@PutMapping("/{id}")
	public EventTypeResponse update(@PathVariable long id, @Valid @RequestBody EventTypeRequest request) {
		return service.update(id, request);
	}
}
