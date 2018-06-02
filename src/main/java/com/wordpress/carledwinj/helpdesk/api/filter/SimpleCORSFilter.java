package com.wordpress.carledwinj.helpdesk.api.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCORSFilter implements Filter {

	private final Log logger = LogFactory.getLog(this.getClass());
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("HelpDesk-API | SimpleCORSFilter loaded.");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
		httpServletResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
		httpServletResponse.setHeader("Access-Control-Max-Age", "3600");
		httpServletResponse.setHeader("Access-Control-Allow-Headers",  "x-requested-with, authorization, Content-Type, Authorization");
		
		if("OPTIONS".equalsIgnoreCase(httpServletRequest.getMethod())) {
			httpServletResponse.setStatus(HttpServletResponse.SC_OK);
		}else {
			chain.doFilter(request, response);
		}
		
	}

	@Override
	public void destroy() {
	}

}
