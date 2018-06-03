package com.wordpress.carledwinj.helpdesk.api.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.wordpress.carledwinj.helpdesk.api.entity.ChangeStatus;
import com.wordpress.carledwinj.helpdesk.api.entity.Ticket;
import com.wordpress.carledwinj.helpdesk.api.entity.User;
import com.wordpress.carledwinj.helpdesk.api.enums.StatusEnum;
import com.wordpress.carledwinj.helpdesk.api.response.Response;
import com.wordpress.carledwinj.helpdesk.api.security.jwt.JwtTokenUtil;
import com.wordpress.carledwinj.helpdesk.api.service.TicketService;
import com.wordpress.carledwinj.helpdesk.api.service.UserService;

@RestController
@CrossOrigin(origins="*")
public class TicketController {

	@Autowired
	private TicketService ticketService;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private UserService userService;
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<String>> deleteById(@PathVariable("id") String id,	 HttpServletRequest httpServletRequest, BindingResult bindingResult){
		
		Response<String> response = new Response<>();
		
		try {
			
			Ticket ticket = ticketService.findById(id);
			
			if(ticket == null) {
				response.getErros().add("Register not Found. Id: " + id);
				return ResponseEntity.badRequest().body(response);
			}
			
			ticketService.delete(id);
			
			response.setData("Success exclusion.");
			
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			
			response.getErros().add("Ticket erro: " + e.getMessage() + ", cause: " + e.getCause());
			return ResponseEntity.badRequest().body(response);
		}
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Ticket>> findById(@PathVariable("id") String id,	 HttpServletRequest httpServletRequest, BindingResult bindingResult){
		
		Response<Ticket> response = new Response<>();
		
		try {
			
			Ticket ticket = ticketService.findById(id);
			
			if(ticket == null) {
				response.getErros().add("Register not Found. Id: " + id);
				return ResponseEntity.badRequest().body(response);
			}
			
			List<ChangeStatus> changes = new ArrayList<>();
			Iterable<ChangeStatus> changesCurrentTicket = ticketService.listChangeStatus(ticket.getId());
			
			for(ChangeStatus changeStatus : changesCurrentTicket) {
				changes.add(changeStatus);
			}
			
			ticket.setChanges(changes);
			
			response.setData(ticket);
			
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			
			response.getErros().add("Ticket erro: " + e.getMessage() + ", cause: " + e.getCause());
			return ResponseEntity.badRequest().body(response);
		}
	}

	
	@PostMapping
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<Ticket>> create(HttpServletRequest httpServletRequest, @RequestBody Ticket ticket, BindingResult bindingResult){
		
		Response<Ticket> response = new Response<>();
		
		try {
			
			validateCreateTicket(ticket, bindingResult);
			
			if(bindingResult.hasErrors()) {
				
				bindingResult.getAllErrors().forEach(error -> response.getErros().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			ticket.setStatus(StatusEnum.getStatus("New"));
			ticket.setUser(getUserFromRequest(httpServletRequest));
			ticket.setDate(new Date());
			ticket.setNumber(generateNumberToTicket());
			
			Ticket ticketPersisted = ticketService.createOrUpdate(ticket);
			
			response.setData(ticketPersisted);
			
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			
			response.getErros().add("Ticket erro: " + e.getMessage() + ", cause: " + e.getCause());
			return ResponseEntity.badRequest().body(response);
		}
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<Ticket>> update(HttpServletRequest httpServletRequest, @RequestBody Ticket ticket, BindingResult bindingResult){
		
		Response<Ticket> response = new Response<>();
		
		try {
			
			validateUpdateTicket(ticket, bindingResult);
			
			if(bindingResult.hasErrors()) {
				
				bindingResult.getAllErrors().forEach(error -> response.getErros().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			ticket.setStatus(StatusEnum.getStatus("New"));
			ticket.setUser(getUserFromRequest(httpServletRequest));
			ticket.setDate(new Date());
			ticket.setNumber(generateNumberToTicket());
			
			Ticket currentTicket = ticketService.findById(ticket.getId());
			
			ticket.setStatus(currentTicket.getStatus());
			ticket.setUser(currentTicket.getUser());
			ticket.setDate(currentTicket.getDate());
			ticket.setNumber(currentTicket.getNumber());
			
			if(currentTicket.getAssignedUser() != null) {
				ticket.setAssignedUser(currentTicket.getAssignedUser());
			}
			
			Ticket ticketPersisted = ticketService.createOrUpdate(ticket);
			
			response.setData(ticketPersisted);
			
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			
			response.getErros().add("Ticket erro: " + e.getMessage() + ", cause: " + e.getCause());
			return ResponseEntity.badRequest().body(response);
		}
	}
	
	private Integer generateNumberToTicket() {
		return (new Random()).nextInt(9999);
	}

	private User getUserFromRequest(HttpServletRequest httpServletRequest) {
		
		String token = httpServletRequest.getHeader("Autorization");
		String email = jwtTokenUtil.getUsernameFromToken(token);
		
		return userService.findByEmail(email);
	}

	private void validateCreateTicket(Ticket ticket, BindingResult bindingResult) {
		
		if(ticket.getTitle() == null) {
			bindingResult.addError(new ObjectError("Ticket", "No informed Ticket."));
		}
	}
	
	private void validateUpdateTicket(Ticket ticket, BindingResult bindingResult) {
		
		if(ticket.getTitle() == null) {
			bindingResult.addError(new ObjectError("Ticket", "No informed Ticket."));
		}
		
		if(ticket.getId() == null) {
			bindingResult.addError(new ObjectError("Ticket", "No informed Ticket Id."));
		}
	}
}
