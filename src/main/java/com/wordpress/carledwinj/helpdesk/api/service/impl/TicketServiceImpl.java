package com.wordpress.carledwinj.helpdesk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wordpress.carledwinj.helpdesk.api.entity.ChangeStatus;
import com.wordpress.carledwinj.helpdesk.api.entity.Ticket;
import com.wordpress.carledwinj.helpdesk.api.repository.ChangeStatusRepository;
import com.wordpress.carledwinj.helpdesk.api.repository.TicketRepository;
import com.wordpress.carledwinj.helpdesk.api.service.TicketService;

@Service
public class TicketServiceImpl implements TicketService {

	@Autowired
	private TicketRepository ticketRepository;
	
	@Autowired
	private ChangeStatusRepository changeStatusRepository;
	
	@Override
	public Ticket createOrUpdate(Ticket ticket) {
		return ticketRepository.save(ticket);
	}

	@Override
	public Ticket findById(String id) {
		return ticketRepository.findOne(id);
	}

	@Override
	public void delete(String id) {
		ticketRepository.delete(id);
	}

	@Override
	public Page<Ticket> listTicket(int page, int count) {
		
		Pageable pages = new PageRequest(page, count);
		return ticketRepository.findAll(pages);
	}

	@Override
	public ChangeStatus createChangeStatus(ChangeStatus changeStatus) {
		return changeStatusRepository.save(changeStatus);
	}

	@Override
	public Iterable<ChangeStatus> listChangeStatus(String ticketId) {
		return changeStatusRepository.findByTicketIdOrderByDateChangeStatusDesc(ticketId);
	}

	@Override
	public Page<Ticket> findByCurrentUser(int page, int count, String currentUserId) {
		
		Pageable pages = new PageRequest(page, count);
		return ticketRepository.findByUserIdOrderByDateDesc(pages, currentUserId);
	}

	@Override
	public Page<Ticket> findByParameters(int page, int count, String title, String status, String priority) {
		Pageable pages = new PageRequest(page, count);
		return ticketRepository.findByTitleIgnoreCaseContainingAndStatusAndPriorityOrderByDateDesc(title, status, priority, pages);
	}

	@Override
	public Page<Ticket> findByParametersCurrentUser(int page, int count, String title, String status, String priority, String currentUserId) {
		Pageable pages = new PageRequest(page, count);
		return ticketRepository.findByTitleIgnoreCaseContainingAndStatusAndPriorityAndUserIdOrderByDateDesc(title, status, priority, currentUserId, pages);
	}

	@Override
	public Page<Ticket> findByNumber(int page, int count, Integer number) {
		Pageable pages = new PageRequest(page, count);
		return ticketRepository.findByNumber(number, pages);
	}

	@Override
	public Iterable<Ticket> findAll() {
		return ticketRepository.findAll();
	}

	@Override
	public Page<Ticket> findByParametersAndAssignedUser(int page, int count, String title, String status, String priority, String assignedUserId) {
		Pageable pages = new PageRequest(page, count);
		return ticketRepository.findByTitleIgnoreCaseContainingAndStatusAndPriorityAndAssignedUserIdOrderByDateDesc(title, status, priority, assignedUserId, pages);
	}

}
