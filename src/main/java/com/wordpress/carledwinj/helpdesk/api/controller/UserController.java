package com.wordpress.carledwinj.helpdesk.api.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import com.mongodb.DuplicateKeyException;
import com.wordpress.carledwinj.helpdesk.api.entity.User;
import com.wordpress.carledwinj.helpdesk.api.response.Response;
import com.wordpress.carledwinj.helpdesk.api.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins="*")
public class UserController {

	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping(value = "/{page}/{count}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<Page<User>>> findAll(@PathVariable("page") int page, @PathVariable("count") int count) {

		Response<Page<User>> response = new Response<>();

		Page<User> users = userService.findAll(page, count);

		response.setData(users);

		return ResponseEntity.ok().body(response);
	}

	
	@GetMapping(value ="/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<User>> findById(@PathVariable("id") String id){
		
		Response<User> response = new Response<>();
		
		try {
			
			User user = userService.findById(id);
			
			if(user == null) {
				response.getErros().add("Register not found. Id: " + id);
				return ResponseEntity.badRequest().body(response);
			}
			
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			return getResponseEntityException(response, e);
		}
	}

	@DeleteMapping(value ="/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<String>> delteById(@PathVariable("id") String id){
		
		Response<String> response = new Response<>();
		
		try {
			
			User user = userService.findById(id);
			
			if(user == null) {
				response.getErros().add("Register not found. Id: " + id);
				return ResponseEntity.badRequest().body(response);
			}
			
			userService.delete(id);
			
			return ResponseEntity.ok().body(new Response<String>());
		} catch (Exception e) {
			response.getErros().add("Internal server error: " + e.getMessage() + ", cause: "+ e.getCause());
			return ResponseEntity.badRequest().body(response);
		}
	}
	private ResponseEntity<Response<User>> getResponseEntityException(Response<User> response, Exception e) {
		response.getErros().add("Internal server error: " + e.getMessage() + ", cause: "+ e.getCause());
		return ResponseEntity.badRequest().body(response);
	}
	
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<User>> create(HttpServletRequest httpServletRequest, @RequestBody User user, BindingResult bindingResult){
	
		Response<User> response = new Response<>();
		
		try {
			
			validateCreateUser(user, bindingResult);
			
			if(bindingResult.hasErrors()) {
				bindingResult.getAllErrors().forEach(error -> response.getErros().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			User userPersisted = userService.createOrUpdate(user);
			
			response.setData(userPersisted);
			return ResponseEntity.ok(response);
		} catch(DuplicateKeyException e){
			
			response.getErros().add("Email already registered.");
			return ResponseEntity.badRequest().body(response);
		} catch (Exception e) {
			return getResponseEntityException(response, e);
		}
	}
	
	
	@PutMapping
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<User>> update(HttpServletRequest httpServletRequest, @RequestBody User user, BindingResult bindingResult){
		
		Response<User> response = new Response<>();
		
		try {
			
			validateUpdateUser(user, bindingResult);
			
			if(bindingResult.hasErrors()) {
				
				response.getErros().forEach(error -> response.getErros().add(error));
				return ResponseEntity.badRequest().body(response);
			}
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			User userPersisted = userService.createOrUpdate(user);
			
			response.setData(userPersisted);
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return getResponseEntityException(response, e);
		}
	}
	
	private void validateCreateUser(User user, BindingResult bindingResult) {
		
		if(user.getEmail() == null) {
			bindingResult.addError(new ObjectError("User", "No informed email."));
		}
	}
	
	private void validateUpdateUser(User user, BindingResult bindingResult) {
		
		if(user.getEmail() == null) {
			bindingResult.addError(new ObjectError("User", "No informed email."));
		}
		
		if(user.getId() == null) {
			bindingResult.addError(new ObjectError("User", "No informed id."));
		}
	}
	
}
