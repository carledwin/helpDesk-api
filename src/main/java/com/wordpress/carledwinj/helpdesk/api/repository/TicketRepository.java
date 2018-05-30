package com.wordpress.carledwinj.helpdesk.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.wordpress.carledwinj.helpdesk.api.entity.Ticket;

public interface TicketRepository extends MongoRepository<Ticket, String> {

	
	//Containing = Like
	Page<Ticket> findByUserIdOrderByDateDesc(Pageable pages, String userId);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusAndPriorityOrderByDateDesc(String titulo, String status, String priority, Pageable pages);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusAndPriorityAndUserIdOrderByDateDesc(String titulo, String status, String priority, Pageable pages);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusAndPriorityAndAssignedUserIdOrderByDateDesc(String titulo, String status, String priority, Pageable pages);
	
	Page<Ticket> findByNumber(Integer number, Pageable pages);
}
