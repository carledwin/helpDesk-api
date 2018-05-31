package com.wordpress.carledwinj.helpdesk.api.security.jwt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.wordpress.carledwinj.helpdesk.api.entity.User;
import com.wordpress.carledwinj.helpdesk.api.enums.ProfileEnum;

/** Converte o perfil do usuario para o padrao utilizado pelo spring security
 * 
 * @author carledwin
 *
 */
public class JwtUSerFactory {

	private JwtUSerFactory() {
	}
	
	public static JwtUser create(User user) {
		return new JwtUser(user.getId(), user.getEmail(), user.getPassword(), mapToGrantedAuthorities(user.getProfile()));
	}

	private static List<GrantedAuthority> mapToGrantedAuthorities(ProfileEnum profileEnum) {
		
		List<GrantedAuthority>  authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(profileEnum.toString()));
		return authorities;
	}
}
