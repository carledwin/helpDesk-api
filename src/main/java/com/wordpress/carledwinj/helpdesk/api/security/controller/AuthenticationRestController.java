package com.wordpress.carledwinj.helpdesk.api.security.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.wordpress.carledwinj.helpdesk.api.entity.User;
import com.wordpress.carledwinj.helpdesk.api.security.jwt.JwtAuthenticationRequest;
import com.wordpress.carledwinj.helpdesk.api.security.jwt.JwtTokenUtil;
import com.wordpress.carledwinj.helpdesk.api.security.model.CurrentUser;
import com.wordpress.carledwinj.helpdesk.api.service.UserService;

@RestController
@CrossOrigin(origins="*")
public class AuthenticationRestController {

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private UserService userService;
	
	@PostMapping(value="/api/auth")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest jwtAuthenticationRequest) throws AuthenticationException{
		
		final Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(jwtAuthenticationRequest.getEmail(), jwtAuthenticationRequest.getPassword()));
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		final UserDetails userDetails = userDetailsService.loadUserByUsername(jwtAuthenticationRequest.getEmail());
		
		final String token = jwtTokenUtil.generateToken(userDetails);
		
		final User user = userService.findByEmail(jwtAuthenticationRequest.getEmail());
		user.setPassword(null);
		
		return ResponseEntity.ok(new CurrentUser(token, user));
	}
	
	@PostMapping(value="/api/refresh")
	public ResponseEntity<?> refreshAndAuthenticationToken(HttpServletRequest httpServletRequest){
		
		String token = httpServletRequest.getHeader("Authorization");
		String username = jwtTokenUtil.getUsernameFromToken(token);
		final User user = userService.findByEmail(username);
		
		if(jwtTokenUtil.canBeRefreshToken(token)) {
			
			String refreshedToken = jwtTokenUtil.refreshToken(token);
			return ResponseEntity.ok(new CurrentUser(refreshedToken, user));
		}else {
			return ResponseEntity.badRequest().body(null);
		}
	}
}
