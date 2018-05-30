package com.wordpress.carledwinj.helpdesk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wordpress.carledwinj.helpdesk.api.entity.User;
import com.wordpress.carledwinj.helpdesk.api.repository.UserRepository;
import com.wordpress.carledwinj.helpdesk.api.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public User createOrUpdate(User user) {
		return userRepository.save(user);
	}

	@Override
	public User findById(String id) {
		return userRepository.findOne(id);
	}

	@Override
	public void delete(String id) {
		userRepository.delete(id);
	}

	@Override
	public Page<User> findAll(int page, int count) {
		Pageable pages = new PageRequest(page, count);
		return userRepository.findAll(pages);
	}

}
