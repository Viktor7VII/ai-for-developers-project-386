package com.hexlet.backend.schedule;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

	private final ScheduleService service;

	public ScheduleController(ScheduleService service) {
		this.service = service;
	}

	@GetMapping
	public ScheduleDto get() {
		return service.get();
	}

	@PutMapping
	public ScheduleDto update(@Valid @RequestBody ScheduleDto request) {
		return service.update(request);
	}
}
