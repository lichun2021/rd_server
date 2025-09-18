package com.hawk.game.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hawk.log.HawkLog;

import com.hawk.game.GsApp;

public class GameStatusFilter implements Filter {
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest rqeuest, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		if (!GsApp.getInstance().isInitOK()) {
			if (rqeuest instanceof HttpServletRequest ) {
				HttpServletRequest httpReqeust = (HttpServletRequest)rqeuest;
				HawkLog.logPrintln("request query string:{}", httpReqeust.getQueryString());
			} else {
				HawkLog.logPrintln("request :{}", rqeuest.toString());
			}
			
			HttpServletResponse resp = (HttpServletResponse) response;
			resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "service unavailable waiting start finish");
		} else {
			filterChain.doFilter(rqeuest, response);
		}
	}
}
