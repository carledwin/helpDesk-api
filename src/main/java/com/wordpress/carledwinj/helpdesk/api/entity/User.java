package com.wordpress.carledwinj.helpdesk.api.entity;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.wordpress.carledwinj.helpdesk.api.enums.ProfileEnum;

@Document
public class User {

	@Id
	private String id;
	
	@Indexed(unique=true)
	@NotBlank(message="Email is required")
	@Email(message="Email is invalid")
	private String email;
	
	@NotBlank(message="Password is required")
	@Size(min=6, max=10, message="Password must be between 6 and 8")
	private String password;
	
	private ProfileEnum profile;

	public User() {
	}
	
	public User(String email, String password, ProfileEnum profile) {
		this.email = email;
		this.password = password;
		this.profile = profile;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ProfileEnum getProfile() {
		return profile;
	}

	public void setProfile(ProfileEnum profile) {
		this.profile = profile;
	}
	
}
