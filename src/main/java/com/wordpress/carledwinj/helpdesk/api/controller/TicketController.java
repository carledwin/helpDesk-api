package com.wordpress.carledwinj.helpdesk.api.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wordpress.carledwinj.helpdesk.api.dto.Summary;
import com.wordpress.carledwinj.helpdesk.api.entity.ChangeStatus;
import com.wordpress.carledwinj.helpdesk.api.entity.Ticket;
import com.wordpress.carledwinj.helpdesk.api.entity.User;
import com.wordpress.carledwinj.helpdesk.api.enums.ProfileEnum;
import com.wordpress.carledwinj.helpdesk.api.enums.StatusEnum;
import com.wordpress.carledwinj.helpdesk.api.response.Response;
import com.wordpress.carledwinj.helpdesk.api.security.jwt.JwtTokenUtil;
import com.wordpress.carledwinj.helpdesk.api.service.TicketService;
import com.wordpress.carledwinj.helpdesk.api.service.UserService;

@RestController
@RequestMapping("/api/ticket")
@CrossOrigin(origins="*")
public class TicketController {

	@Autowired
	private TicketService ticketService;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private UserService userService;

	@GetMapping(value="/summary")
	public ResponseEntity<Response<Summary>> findSummary(){
		
		Response<Summary> response = new Response<>();
		
		Summary summary = new Summary();
		
		int amountNew = 0;
		int amountResolved = 0;
		int amountApproved = 0;
		int amountDisapproved = 0;
		int amountAssigned = 0;
		int amountClosed = 0;
		
		try {
			
			
			Iterable<Ticket> tickets = ticketService.findAll();
			
			if(tickets != null) {
				for (Ticket ticket : tickets) {
					if(ticket.getStatus().equals(StatusEnum.New)) {
						amountNew++;
					}else if(ticket.getStatus().equals(StatusEnum.Resolved)) {
						amountResolved++;
					}else if(ticket.getStatus().equals(StatusEnum.Approved)) {
						amountApproved++;
					}else if(ticket.getStatus().equals(StatusEnum.Disapproved)) {
						amountDisapproved++;
					}else if(ticket.getStatus().equals(StatusEnum.Assigned)) {
						amountAssigned++;
					}else if(ticket.getStatus().equals(StatusEnum.Closed)) {
						amountClosed++;
					}	
				}
			}

			summary.setAmountNew(amountNew);
			summary.setAmountResolved(amountResolved);
			summary.setAmountApproved(amountApproved);
			summary.setAmountDisapproved(amountDisapproved);
			summary.setAmountAssigned(amountAssigned);
			summary.setAmountClosed(amountClosed);
			
			
			response.setData(summary);
			
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			
			response.getErros().add("Summary erro: " + e.getMessage() + ", cause: " + e.getCause());
			return ResponseEntity.badRequest().body(response);
		}
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Ticket>> findById(@PathVariable("id") String id,	 HttpServletRequest httpServletRequest){
		
		Response<Ticket> response = new Response<>();
		
		try {
			
			Ticket ticket = ticketService.findById(id);
			
			if(ticket == null) {
				response.getErros().add("Register not Found. Id: " + id);
				return ResponseEntity.badRequest().body(response);
			}
			
			findChangesToTicket(ticket);
			
			response.setData(ticket);
			
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			
			response.getErros().add("Ticket erro: " + e.getMessage() + ", cause: " + e.getCause());
			return ResponseEntity.badRequest().body(response);
		}
	}
	
	@GetMapping("/{page}/{count}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Page<Ticket>>> findAll(@PathVariable("page") int page,	@PathVariable("count") int count, HttpServletRequest httpServletRequest){
		
		Response<Page<Ticket>> response = new Response<>();
		Page<Ticket> tickets = null;
		
		try {
			
			User userRequest = getUserFromRequest(httpServletRequest);
			
			if(userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
				tickets = ticketService.listTicket(page, count);
			} else if(userRequest.getProfile().equals(ProfileEnum.ROLE_CUSTOMER)) {
				tickets = ticketService.findByCurrentUser(page, count, userRequest.getId());
			}
			
			
			if(tickets == null) {
				response.getErros().add("No records Found.");
				return ResponseEntity.badRequest().body(response);
			}
			
			for(Ticket ticket : tickets) {
				findChangesToTicket(ticket);
			}
			
			response.setData(tickets);
			
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			
			response.getErros().add("Ticket erro: " + e.getMessage() + ", cause: " + e.getCause());
			return ResponseEntity.badRequest().body(response);
		}
	}
	
	@GetMapping("/{page}/{count}/{number}/{title}/{status}/{priority}/{assigned}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Page<Ticket>>> findAllByParameters(@PathVariable("page") int page,	
																	 @PathVariable("count") int count, 
																	 @PathVariable("number") int number, 
																	 @PathVariable("title") String title, 
																	 @PathVariable("status") String status, 
																	 @PathVariable("priority") String priority, 
																	 @PathVariable("assigned") String assigned, 
																	 HttpServletRequest httpServletRequest){
		
		Response<Page<Ticket>> response = new Response<>();
		Page<Ticket> tickets = null;
		boolean findByAssignedUser = false;
		
		try {
			
			title = title.equals("uninformed") ? "" : title;
			status = status.equals("uninformed") ? "" : status;
			priority = priority.equals("uninformed") ? "" : priority;
			
			User userRequest = getUserFromRequest(httpServletRequest);
			
			
			if(number > 0) {
				tickets = ticketService.findByNumber(page, count, number);
			}else {
			
				if(userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
					
					findByAssignedUser = getAssigned(assigned, findByAssignedUser);
					
					if(findByAssignedUser ) {
						tickets = ticketService.findByParametersAndAssignedUser(page, count, title, status, priority, userRequest.getId());
					}else {
						tickets = ticketService.findByParameters(page, count, title, status, priority);
					}	
				}else if(userRequest.getProfile().equals(ProfileEnum.ROLE_CUSTOMER)) {
					tickets = ticketService.findByParametersCurrentUser(page, count, title, status, priority, userRequest.getId());
				}
			}
			
			if(tickets == null) {
				response.getErros().add("No records Found.");
				return ResponseEntity.badRequest().body(response);
			}
			
			for(Ticket ticket : tickets) {
				findChangesToTicket(ticket);
			}
			
			response.setData(tickets);
			
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
	
	@PutMapping
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

	@PutMapping(value="/{id}/{status}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Ticket>> changeStatus(@PathVariable("id") String id, @PathVariable("status") String status, HttpServletRequest httpServletRequest, @RequestBody Ticket ticket, BindingResult bindingResult){
		
		Response<Ticket> response = new Response<>();
		
		try {
			
			validateChangeStatusTicket(id, status, bindingResult);
			
			if(bindingResult.hasErrors()) {
				bindingResult.getAllErrors().forEach(error -> response.getErros().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			Ticket currentTicket = ticketService.findById(id);
			currentTicket.setStatus(StatusEnum.getStatus(status));
			
			if(status.equals("Assigned")) {		
				currentTicket.setUser(getUserFromRequest(httpServletRequest));
			}
			
			Ticket ticketPersisted = ticketService.createOrUpdate(currentTicket);
			
			ChangeStatus changeStatus = new ChangeStatus();
			changeStatus.setUserChange(getUserFromRequest(httpServletRequest));
			changeStatus.setStatus(StatusEnum.getStatus(status));
			changeStatus.setTicket(ticketPersisted);
			
			ticketService.createChangeStatus(changeStatus);
			
			response.setData(ticketPersisted);
			
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			
			response.getErros().add("Ticket erro: " + e.getMessage() + ", cause: " + e.getCause());
			return ResponseEntity.badRequest().body(response);
		}
	}

	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<String>> deleteById(@PathVariable("id") String id, HttpServletRequest httpServletRequest){
		
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

	
	private Integer generateNumberToTicket() {
		return (new Random()).nextInt(9999);
	}



	private void findChangesToTicket(Ticket ticket) {
		List<ChangeStatus> changes = new ArrayList<>();
		Iterable<ChangeStatus> changesCurrentTicket = ticketService.listChangeStatus(ticket.getId());
		
		for(ChangeStatus changeStatus : changesCurrentTicket) {
			changes.add(changeStatus);
		}
		
		ticket.setChanges(changes);
	}

	
	private User getUserFromRequest(HttpServletRequest httpServletRequest) {
		
		String token = httpServletRequest.getHeader("Authorization");
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

	private void validateChangeStatusTicket(String id, String status, BindingResult bindingResult) {
	
		if(id == null || id.equals("")) {
			bindingResult.addError(new ObjectError("Ticket", "No informed Ticket Id."));
		}
		
		if(status == null || status.equals("")) {
			bindingResult.addError(new ObjectError("Ticket", "No informed Status."));
		}
	}

	private boolean getAssigned(String assigned, boolean findByAssignedUser) {
		if(assigned != null ) {
			assigned = assigned.trim();
			
			if(assigned.equalsIgnoreCase("true") || assigned.equalsIgnoreCase("false")) {
				findByAssignedUser = Boolean.parseBoolean(assigned);
			}
		}
		return findByAssignedUser;
	}

}
