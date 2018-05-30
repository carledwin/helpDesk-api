package com.wordpress.carledwinj.helpdesk.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.wordpress.carledwinj.helpdesk.api.entity.ChangeStatus;

public interface ChangeStatusRepository extends MongoRepository<ChangeStatus, String> {

	Iterable<ChangeStatus> findByTicketIdOrderByDateChangeStatusDesc(String ticketId);
}
