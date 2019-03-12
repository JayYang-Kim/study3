package com.guest;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.member.SessionInfo;
import com.util.MyServlet;
import com.util.MyUtil;

//@WebServlet("/guest/*")
public class GuestServlet2 extends MyServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		
		String uri=req.getRequestURI();
		
		// uri에 따른 작업 구분
		if(uri.indexOf("guest.do")!=-1) {
			guest(req, resp);
		} else if(uri.indexOf("guest_ok.do")!=-1) {
			guestSubmit(req, resp);
		} else if(uri.indexOf("delete.do")!=-1) {
			delete(req, resp);
		}
	}

	private void guest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 방명록 리스트
		String cp=req.getContextPath();
		
		GuestDAO dao=new GuestDAO();
		MyUtil util=new MyUtil();
		
		// 넘어온 페이지
		String page=req.getParameter("page");
		int current_page=1;
		if(page!=null && page.length()!=0)
			current_page=Integer.parseInt(page);
		
		// 전체 데이터 개수
		int dataCount=dao.dataCount();
		
		// 전체페이지수 구하기
		int rows=5;
		int total_page=util.pageCount(rows, dataCount);
		
		// 전체페이지보다 표시할 페이지가 큰경우
		if(total_page<current_page)
			current_page=total_page;
		
		// 가져올데이터의 시작과 끝
		int start=(current_page-1)*rows+1;
		int end=current_page*rows;
		
		// 데이터 가져오기
		List<GuestDTO> list=dao.listGuest(start, end);
		
		Iterator<GuestDTO> it=list.iterator();
		while (it.hasNext()) {
			GuestDTO dto=it.next();
			
			dto.setContent(dto.getContent().replaceAll(">", "&gt;"));
			dto.setContent(dto.getContent().replaceAll("<", "&lt;"));
			dto.setContent(dto.getContent().replaceAll("\n", "<br>"));
		}
		
		// 페이징처리
		String strUrl=cp+"/guest/guest.do";
		String paging=util.paging(	current_page, total_page, strUrl);
		
		// guest.jsp에 넘겨줄 데이터
		req.setAttribute("list", list);
		req.setAttribute("page", current_page);
		req.setAttribute("total_page", total_page);
		req.setAttribute("paging", paging);
		req.setAttribute("dataCount", dataCount);
		
		forward(req, resp, "/WEB-INF/views/guest/guest.jsp");
	}
	
	private void guestSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 방명록 저장
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		String cp=req.getContextPath();
		
		if(info==null) { // 로그인되지 않은 경우
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		GuestDAO dao=new GuestDAO();
		GuestDTO dto=new GuestDTO();
		
		dto.setUserId(info.getUserId());
		dto.setContent(req.getParameter("content"));
		
		dao.insertGuest(dto);
		
		resp.sendRedirect(cp+"/guest/guest.do");
	}
	
	private void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 방명록 삭제
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		String cp=req.getContextPath();
		
		if(info==null) { // 로그인되지 않은 경우
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		GuestDAO dao=new GuestDAO();
		
		int num=Integer.parseInt(req.getParameter("num"));
		String page=req.getParameter("page");

		dao.deleteGuest(num, info.getUserId());
		
		resp.sendRedirect(cp+"/guest/guest.do?page="+page);
	}
}
