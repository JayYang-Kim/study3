package com.bbs;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

@WebServlet("/bbs/*")
public class BoardServlet extends MyServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		
		String uri=req.getRequestURI();
		
		// 세션 정보
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		if(info==null) {
			forward(req, resp, "/WEB-INF/views/member/login.jsp");
			return;
		}
		
		// uri에 따른 작업 구분
		if(uri.indexOf("list.do")!=-1) {
			list(req, resp);
		} else if(uri.indexOf("created.do")!=-1) {
			createdForm(req, resp);
		} else if(uri.indexOf("created_ok.do")!=-1) {
			createdSubmit(req, resp);
		} else if(uri.indexOf("article.do")!=-1) {
			article(req, resp);
		} else if(uri.indexOf("update.do")!=-1) {
			updateForm(req, resp);
		} else if(uri.indexOf("update_ok.do")!=-1) {
			updateSubmit(req, resp);
		} else if(uri.indexOf("delete.do")!=-1) {
			delete(req, resp);
		}
	}

	private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 게시물 리스트
		String cp = req.getContextPath();

		BoardDAO dao = new BoardDAO();
		MyUtil util = new MyUtil();
	
		String page=req.getParameter("page");
		int current_page=1;
		if(page!=null)
			current_page=Integer.parseInt(page);
		
		// 검색
		String searchKey=req.getParameter("searchKey");
		String searchValue=req.getParameter("searchValue");
		if(searchKey==null) {
			searchKey="subject";
			searchValue="";
		}
		
		// GET 방식인 경우 디코딩
		if(req.getMethod().equalsIgnoreCase("GET")) {
			searchValue=URLDecoder.decode(searchValue, "utf-8");
		}
		
		// 전체 데이터 개수
		int dataCount;
		if(searchValue.length()==0)
			dataCount=dao.dataCount();
		else
			dataCount=dao.dataCount(searchKey, searchValue);
		
		// 전체 페이지 수
		int rows=10;
		int total_page=util.pageCount(rows, dataCount);
		
		if(current_page>total_page)
			current_page=total_page;
		
		// 게시물 가져올 시작과 끝
		int start=(current_page-1)*rows+1;
		int end=current_page*rows;
		
		// 게시물 가져오기
		List<BoardDTO> list=null;
		if(searchValue.length()==0)
			list=dao.listBoard(start, end);
		else
			list=dao.listBoard(start, end, searchKey, searchValue);
		
		// 리스트 글번호 만들기
		int listNum, n=0;
		Iterator<BoardDTO>it=list.iterator();
		while(it.hasNext()) {
			BoardDTO dto=it.next();
			listNum=dataCount-(start+n-1);
			dto.setListNum(listNum);
			n++;
		}
		
		String query="";
		if(searchValue.length()!=0) {
			// 검색인 경우 검색값 인코딩
			searchValue=URLEncoder.encode(searchValue, "utf-8");
			query="searchKey="+searchKey+
					 "&searchValue="+searchValue;
		}
		
		// 페이징 처리
		String listUrl=cp+"/bbs/list.do";
		String articleUrl=cp+"/bbs/article.do?page="+current_page;
		if(query.length()!=0) {
			listUrl+="?"+query;
			articleUrl+="&"+query;
		}
		
		String paging=util.paging(current_page, total_page, listUrl);
		
		// 포워딩할 JSP로 넘길 속성
		req.setAttribute("list", list);
		req.setAttribute("page", current_page);
		req.setAttribute("total_page", total_page);
		req.setAttribute("dataCount", dataCount);
		req.setAttribute("articleUrl", articleUrl);
		req.setAttribute("paging", paging);
		
		// JSP로 포워딩
		forward(req, resp, "/WEB-INF/views/bbs/list.jsp");
	}
	
	private void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글쓰기 폼
		req.setAttribute("mode", "created");
		forward(req, resp, "/WEB-INF/views/bbs/created.jsp");
	}
	
	private void createdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 저장
		String cp = req.getContextPath();

		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		BoardDAO dao = new BoardDAO();
		BoardDTO dto=new BoardDTO();
		
		// userId는 세션에 저장된 정보
		dto.setUserId(info.getUserId());
		
		// 파라미터
		dto.setSubject(req.getParameter("subject"));
		dto.setContent(req.getParameter("content"));
		
		dao.insertBoard(dto);
		
		resp.sendRedirect(cp+"/bbs/list.do");
	}

	private void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글보기
		String cp = req.getContextPath();
		BoardDAO dao = new BoardDAO();
		MyUtil myUtil = new MyUtil();
	
		int num=Integer.parseInt(req.getParameter("num"));
		String page=req.getParameter("page");
		String searchKey=req.getParameter("searchKey");
		String searchValue=req.getParameter("searchValue");
		if(searchKey==null) {
			searchKey="subject";
			searchValue="";
		}
		
		searchValue=URLDecoder.decode(searchValue, "utf-8");
		
		// 조회수 증가
		dao.updateHitCount(num);
		
		// 게시물 가져오기
		BoardDTO dto=dao.readBoard(num);
		if(dto==null) { // 게시물이 없으면 다시 리스트로
			resp.sendRedirect(cp+"/bbs/list.do?page="+page);
			return;
		}
		dto.setContent(myUtil.htmlSymbols(dto.getContent()));
		
		// 이전글 다음글
		BoardDTO preReadDto=dao.preReadBoard(dto.getNum(), searchKey, searchValue);
		BoardDTO nextReadDto=dao.nextReadBoard(dto.getNum(), searchKey, searchValue);
		
		// 리스트나 이전글/다음글에서 사용할 파라미터
		String query="page="+page;
		if(searchValue.length()!=0) {
			query+="&searchKey="+searchKey
					+"&searchValue="+URLEncoder.encode(searchValue, "utf-8");
		}
		
		// JSP로 전달할 속성
		req.setAttribute("dto", dto);
		req.setAttribute("page", page);
		req.setAttribute("query", query);
		req.setAttribute("preReadDto", preReadDto);
		req.setAttribute("nextReadDto", nextReadDto);
		
		// 포워딩
		forward(req, resp, "/WEB-INF/views/bbs/article.jsp");
	}
	
	private void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 수정 폼
		String cp = req.getContextPath();

		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		BoardDAO dao = new BoardDAO();
	
		String page=req.getParameter("page");
		int num=Integer.parseInt(	req.getParameter("num"));
		BoardDTO dto=dao.readBoard(num);
		
		if(dto==null) {
			resp.sendRedirect(cp+"/bbs/list.do?page="+page);
			return;
		}
		
		// 게시물을 올린 사용자가 아니면
		if(! dto.getUserId().equals(info.getUserId())) {
			resp.sendRedirect(cp+"/bbs/list.do?page="+page);
			return;
		}
		
		req.setAttribute("dto", dto);
		req.setAttribute("page", page);
		req.setAttribute("mode", "update");
		
		forward(req, resp, "/WEB-INF/views/bbs/created.jsp");
	}

	private void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 수정 완료
		String cp = req.getContextPath();
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
	
		BoardDAO dao = new BoardDAO();
		
		String page=req.getParameter("page");
		
		if(req.getMethod().equalsIgnoreCase("GET")) {
			resp.sendRedirect(cp+"/bbs/list.do?page="+page);
			return;
		}
		
		BoardDTO dto=new BoardDTO();
		dto.setNum(Integer.parseInt(req.getParameter("num")));
		dto.setSubject(req.getParameter("subject"));
		dto.setContent(req.getParameter("content"));
		
		dao.updateBoard(dto, info.getUserId());
		
		resp.sendRedirect(cp+"/bbs/list.do?page="+page);
	}

	private void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 삭제
		String cp = req.getContextPath();

		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		BoardDAO dao = new BoardDAO();
	
		String page=req.getParameter("page");
		int num=Integer.parseInt(req.getParameter("num"));
		
		dao.deleteBoard(num, info.getUserId());
		
		resp.sendRedirect(cp+"/bbs/list.do?page="+page);
	}
}
