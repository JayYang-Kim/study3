package com.guest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.member.SessionInfo;
import com.util.MyServlet;
import com.util.MyUtil;

import net.sf.json.JSONObject;

@WebServlet("/guest/*")
public class GuestServlet extends MyServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		
		String uri=req.getRequestURI();
		
		// uri에 따른 작업 구분
		if(uri.indexOf("guest.do")!=-1) { // ajax (x)
			guest(req, resp);
		} else if(uri.indexOf("list.do")!=-1) { // ajax
			list(req, resp);
		} else if(uri.indexOf("insert.do")!=-1) { // ajax
			guestSubmit(req, resp);
		} else if(uri.indexOf("delete.do")!=-1) { // ajax
			delete(req, resp);
		}
	}

	private void guest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		forward(req, resp, "/WEB-INF/views/guest/guest.jsp");
	}
	
	private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 방명록 리스트
		GuestDAO dao = new GuestDAO();
		MyUtil util = new MyUtil();
		
		// 넘어온 페이지
		String page = req.getParameter("pageNo");
		int current_page = 1;
		if(page != null) {
			current_page = Integer.parseInt(page);
		}
		
		int dataCount = dao.dataCount();
		
		int rows = 5;
		int total_page = util.pageCount(rows, dataCount);
		
		if(total_page < current_page) {
			current_page = total_page;
		}
		
		int start = (current_page - 1) * rows + 1;
		int end = current_page * rows;
		
		List<GuestDTO> list = dao.listGuest(start, end);
		
		for(GuestDTO dto : list) {
			dto.setContent(util.htmlSymbols(dto.getContent()));
		}
		
		String paging = util.paging(current_page, total_page);
		
		JSONObject job = new JSONObject();
		job.put("list", list);
		job.put("dataCount", dataCount);
		job.put("total_page", total_page);
		job.put("pageNo", current_page);
		job.put("paging", paging);
		
		resp.setContentType("text/html;charset=utf-8");
		PrintWriter out = resp.getWriter();
		out.print(job.toString());
	}

	private void guestSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 방명록 저장
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		String state = "true";
		if(info == null) {
			state = "loginFail";
		} else {
			// 로그인을 했을 경우
			GuestDAO dao = new GuestDAO();
			GuestDTO dto = new GuestDTO();
			
			dto.setUserId(info.getUserId());
			dto.setContent(req.getParameter("content"));
			
			dao.insertGuest(dto);
		}
		
		JSONObject job = new JSONObject();
		job.put("state", state);
		
		// json으로 결과 전송
		resp.setContentType("text/html;charset=utf-8");
		PrintWriter out = resp.getWriter();
		out.print(job.toString());
	}
	
	private void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 방명록 삭제
		String cp = req.getContextPath();
		
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		String state = "true";
		if(info == null) {
			state = "loginFail";
		} else {
			GuestDAO dao = new GuestDAO();
			
			int num = Integer.parseInt(req.getParameter("num"));
			
			dao.deleteGuest(num, info.getUserId());
		}
		
		JSONObject job = new JSONObject();
		job.put("state", state);
		
		// json으로 결과 전송
		resp.setContentType("text/html;charset=utf-8");
		PrintWriter out = resp.getWriter();
		out.print(job.toString());
	}
}
