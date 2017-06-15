package com.gable.socket.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import com.gable.socket.bean.JsonReturn;
import com.gable.socket.utils.JsonUtil;
import com.gable.socket.utils.MD5Util;

@Order(1)
@WebFilter(filterName = "tokenFilter", urlPatterns = "/socket/socketRequest")
public class TokenFilter implements Filter {

	@Override
	public void destroy() {

	}

	/**
	 * 校验TOKEN
	 */
	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) arg0;
		HttpServletResponse httpResponse = (HttpServletResponse) arg1;
		String requestTime = httpRequest.getHeader("requestTime");
		String token = httpRequest.getHeader("token");
		if(StringUtils.isEmpty(requestTime) || StringUtils.isEmpty(token)){
			String str = JsonUtil.toJsonString(new JsonReturn(0, "_____参数校验失败，requestTime或token为空"));
			httpRequest.setAttribute("error", str);
			httpRequest.getRequestDispatcher("filterError").forward(httpRequest, httpResponse);
			return;
		}
		
		String md5 = MD5Util.md5(requestTime);
		if(!md5.equals(token)){
			String str = JsonUtil.toJsonString(new JsonReturn(0, "_____token校验失败,Atoken:"+token+"Jtoken:"+md5));
			httpRequest.setAttribute("error", str);
			httpRequest.getRequestDispatcher("filterError").forward(httpRequest, httpResponse);
			return;
		}
		arg2.doFilter(httpRequest, httpResponse);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}
}
