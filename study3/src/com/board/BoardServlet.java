package com.board;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.member.SessionInfo;
import com.util.MyUtil;

@WebServlet("/board/*")
public class BoardServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}
	
	protected void forward(HttpServletRequest req, HttpServletResponse resp, String path) throws ServletException, IOException {
		RequestDispatcher rd = req.getRequestDispatcher(path);
		rd.forward(req, resp);
	}
	
	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		if(info == null) {
			forward(req, resp, "/WEB-INF/views/member/login.jsp");
			return;
		}
		
		String uri = req.getRequestURI();
		
		if(uri.indexOf("list.do") != -1) {
			list(req, resp);
		} else if(uri.indexOf("created.do") != -1) {
			createdForm(req, resp);
		} else if(uri.indexOf("created_ok.do") != -1) {
			createdSubmit(req, resp);
		} else if(uri.indexOf("article.do") != -1) {
			article(req, resp);
		} else if(uri.indexOf("update.do") != -1) {
			updateForm(req, resp);
		} else if(uri.indexOf("update_ok.do") != -1) {
			updateSubmit(req, resp);
		} else if(uri.indexOf("reply.do") != -1) {
			replyForm(req, resp);
		} else if(uri.indexOf("reply_ok.do") != -1) {
			replySubmit(req, resp);
		} else if(uri.indexOf("delete.do") != -1) {
			delete(req, resp);
		}
	}
	
	protected void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 리스트
		MyUtil util = new MyUtil();
		BoardDAO dao = new BoardDAO();
		String cp = req.getContextPath();
		
		// 파라미터로 넘어온 페이지 번호
		String page = req.getParameter("page");
		int current_page = 1;
		if(page != null) {
			current_page = Integer.parseInt(page);
		}
		
		// 검색
		String searchKey = req.getParameter("searchKey");
		String searchValue = req.getParameter("searchValue");
		if(searchKey == null) {
			searchKey = "subject";
			searchValue = "";
		}
		
		// GET방식은 검색 문자열을 인코딩해서 파라미터가 보냄으로 다시 디코딩이 필요
		if(req.getMethod().equalsIgnoreCase("GET")) {
			searchValue = URLDecoder.decode(searchValue, "UTF-8");
		}
		
		// 데이터 개수
		int dataCount;
		if(searchValue.length() == 0) {
			dataCount = dao.dataCount();
		} else {
			dataCount = dao.dataCount(searchKey, searchValue);
		}
		
		// 전체 페이지 수
		int rows = 10;
		int total_page = util.pageCount(rows, dataCount);
		if(current_page > total_page) {
			current_page = total_page;
		}
		
		// 게시물 가져오기
		int start = (current_page - 1) * rows + 1;
		int end = current_page * rows;
		
		List<BoardDTO> list;
		if(searchValue.length() == 0) {
			list = dao.listBoard(start, end);
		} else {
			list = dao.listBoard(start, end, searchKey, searchValue);
		}
		
		// 리스트 글번호 만들기
		int listNum, n = 0;
		Iterator<BoardDTO> it = list.iterator();
		while(it.hasNext()) {
			BoardDTO dto = it.next();
			listNum = dataCount - (start + n -1);
			dto.setListNum(listNum);
			n++;
		}
		
		String query = "";
		if(searchValue.length() != 0) {
			query = "searchKey=" + searchKey + "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");
		}
		
		// 페이징 처리
		String listUrl = cp + "/board/list.do";
		String articleUrl = cp + "/board/article.do?page=" + current_page;
		if(query.length() != 0) {
			listUrl += "?" + query;
			articleUrl += "&" + query;
		}
		
		String paging = util.paging(current_page, total_page, listUrl);
		
		req.setAttribute("list", list);
		req.setAttribute("dataCount", dataCount);
		req.setAttribute("page", current_page);
		req.setAttribute("total_page", total_page);
		req.setAttribute("articleUrl", articleUrl);
		req.setAttribute("paging", paging);
		
		forward(req, resp, "/WEB-INF/views/board/list.jsp");
	}
	
	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 등록 폼
		req.setAttribute("mode", "created");
		forward(req, resp, "/WEB-INF/views/board/created.jsp");
	}
	
	protected void createdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 등록
		String cp = req.getContextPath();
		
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		BoardDAO dao = new BoardDAO();
		BoardDTO dto = new BoardDTO();
		dto.setSubject(req.getParameter("subject"));
		dto.setContent(req.getParameter("content"));
		dto.setUserId(info.getUserId());
		
		dao.insertBoard(dto, "created");
		
		resp.sendRedirect(cp + "/board/list.do");
	}
	
	protected void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 보기
		BoardDAO dao = new BoardDAO();
		String cp = req.getContextPath();
		
		int boardNum = Integer.parseInt(req.getParameter("boardNum"));
		String page = req.getParameter("page");
		
		String searchKey = req.getParameter("searchKey");
		String searchValue = req.getParameter("searchValue");
		if(searchKey == null) {
			searchKey = "subject";
			searchValue = "";
		}
		
		searchValue = URLDecoder.decode(searchValue, "UTF-8");
		
		String query = "page=" + page;
		if(searchValue.length() != 0) {
			query += "&searchKey=" + searchKey + "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");
		}
		
		dao.updateHitCount(boardNum);
		BoardDTO dto = dao.readBoard(boardNum);
		if(dto == null) {
			resp.sendRedirect(cp + "/board/list.do?" + query);
			return;
		}
		
		//dto.setContent(dto.getContent().replaceAll("\n", "<br/>"));
		MyUtil util = new MyUtil();
		dto.setContent(util.htmlSymbols(dto.getContent()));
		
		BoardDTO preReadDto = dao.preReadBoard(dto.getGroupNum(), dto.getOrderNo(), searchKey, searchValue);
		BoardDTO nextReadDto = dao.nextReadBoard(dto.getGroupNum(), dto.getOrderNo(), searchKey, searchValue);
		
		req.setAttribute("dto", dto);
		req.setAttribute("preReadDto", preReadDto);
		req.setAttribute("nextReadDto", nextReadDto);
		req.setAttribute("query", query);
		req.setAttribute("page", page);
		
		forward(req, resp, "/WEB-INF/views/board/article.jsp");
	}
	
	protected void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 수정 폼
		
	}
	
	protected void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 수정
		
	}
	
	protected void replyForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 답변 폼
		BoardDAO dao = new BoardDAO();
		String cp = req.getContextPath();
		
		int boardNum = Integer.parseInt(req.getParameter("boardNum"));
		String page = req.getParameter("page");
		
		BoardDTO dto = dao.readBoard(boardNum);
		if(dto == null) {
			resp.sendRedirect(cp + "/board/list.do?page=" + page);
			return;
		}
		
		String s = "[" + dto.getSubject() + "]에 대한 답변입니다.\n";
		dto.setContent(s);
		
		req.setAttribute("mode", "reply");
		req.setAttribute("dto", dto);
		req.setAttribute("page", page);
		
		forward(req, resp, "/WEB-INF/views/board/created.jsp");
	}
	
	protected void replySubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 답변
		String cp = req.getContextPath();
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		BoardDAO dao = new BoardDAO();
		BoardDTO dto = new BoardDTO();
		
		dto.setSubject(req.getParameter("subject"));
		dto.setContent(req.getParameter("content"));
		
		dto.setGroupNum(Integer.parseInt(req.getParameter("groupNum")));
		dto.setOrderNo(Integer.parseInt(req.getParameter("orderNo")));
		dto.setDepth(Integer.parseInt(req.getParameter("depth")));
		dto.setParent(Integer.parseInt(req.getParameter("parent")));
		
		dto.setUserId(info.getUserId());
		
		dao.insertBoard(dto, "reply");
		
		String page = req.getParameter("page");
		
		resp.sendRedirect(cp + "/board/list.do?page=" + page);
	}
	
	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 삭제
		
	}
	
}
