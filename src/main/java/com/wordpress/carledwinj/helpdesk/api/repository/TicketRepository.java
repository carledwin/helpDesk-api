package com.wordpress.carledwinj.helpdesk.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.wordpress.carledwinj.helpdesk.api.entity.Ticket;

public interface TicketRepository extends MongoRepository<Ticket, String> {

	Page<Ticket> findByNumber(Integer number, Pageable pages);
	
	//Containing = Like
	Page<Ticket> findByUserIdOrderByDateDesc(Pageable pages, String userId);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusAndPriorityOrderByDateDesc(String title, String status, String priority, Pageable pages);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusAndPriorityAndUserIdOrderByDateDesc(String title, String status, String priority, String userId, Pageable pages);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusAndPriorityAndAssignedUserIdOrderByDateDesc(String title, String status, String priority, String assignedUserId, Pageable pages);
}
