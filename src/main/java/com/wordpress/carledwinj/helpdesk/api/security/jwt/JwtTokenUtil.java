package com.wordpress.carledwinj.helpdesk.api.security.jwt;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil implements Serializable {

	private static final long serialVersionUID = 1L;
	static final String CLAIM_KEY_USERNAME = "sub";
	static final String CLAIM_KEY_USER_CREATED = "created";
	static final String CLAIM_KEY_USER_EXPIRED = "exp";

	@Value("${jwt.secret}")
	private String secret;

	@Value("${expiration}")
	private Long expiration;

	public String getUsernameFromToken(String token) {

		String username;

		try {
			final Claims claims = getClaimsFromTroken(token);
			username = claims.getSubject();
		} catch (Exception e) {
			username = null;
		}
		return username;
	}

	public Date getEpirationDateFromToken(String token) {

		Date expirationDate;

		try {
			final Claims claims = getClaimsFromTroken(token);
			expirationDate = claims.getExpiration();
		} catch (Exception e) {
			expirationDate = null;
		}

		return expirationDate;
	}

	private Claims getClaimsFromTroken(String token) {

		Claims claims;

		try {
			claims = Jwts
					.parser()
					.setSigningKey(secret)
					.parseClaimsJws(token)
					.getBody();
		} catch (Exception e) {
			claims = null;
		}

		return null;
	}
	
	
	private Boolean isTokenExpired(String token) {
		return getEpirationDateFromToken(token).before(new Date());
	}
	
	public String generateToken(UserDetails userDetails) {
		
		Map<String, Object> claims = new HashMap<>();
				
				claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
		
		claims.put(CLAIM_KEY_USER_CREATED, new Date());
		
		return doGenerateToken(claims);
	}

	private String doGenerateToken(Map<String, Object> claims) {
		
		final Date expirationDate = new Date(((Date) claims.get(CLAIM_KEY_USER_CREATED)).getTime() + expiration + 1000);
		return Jwts
				.builder()
				.setClaims(claims)
				.setExpiration(expirationDate)
				.signWith(SignatureAlgorithm.HS512, secret)
				.compact();
	}
	
	public Boolean canBeRefreshToken(String token) {
		return (isTokenExpired(token));	
	}
	
	public String refreshToken(String token) {
		
		String refreshToken;
		
		try {
			
			final Claims claims = getClaimsFromTroken(token);
			claims.put(CLAIM_KEY_USER_CREATED, new Date());
			refreshToken = doGenerateToken(claims);
		}catch(Exception e) {
			refreshToken = null;
		}
		
		return refreshToken;
	}
	
	public Boolean validateToken(String token, UserDetails userDetails) {
		
		JwtUser jwtUser = (JwtUser) userDetails;
		final String username = getUsernameFromToken(token);
		return (username.equals(jwtUser.getUsername()) && !isTokenExpired(token));
	}
}
