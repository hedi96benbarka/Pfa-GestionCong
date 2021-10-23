package tn.iit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.iit.services.StatsService;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

	@Autowired
	private StatsService statsService;

	@GetMapping("")
	public float getAll() {
		return statsService.getUserStats(getPrincipale());
	}

	private String getPrincipale() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}
}
