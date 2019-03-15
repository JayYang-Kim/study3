package com.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.member.SessionInfo;

@WebFilter("/*")
public class LoginCheckFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest)request;
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		String uri = req.getRequestURI();
		
		String []uris = {"/resource","/index.jsp","/main.do","/member/member.do","/member/userIdCheck.do","/member/login.do","/member/login_ok.do"};
		
		uri = uri.substring(req.getContextPath().length());
		
		if(uri.length() > 1 && info == null) {
			boolean b = false;
			
			for(String s : uris) {
				if(uri.indexOf(s) == 0) {
					b = true;
					break;
				}
			}
			
			if(!b) {
				RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/member/login.jsp");
				rd.forward(request, response);
				return;
			}
		}
		
		chain.doFilter(request, response);
		
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
