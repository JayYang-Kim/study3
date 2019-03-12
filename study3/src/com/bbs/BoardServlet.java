package com.bbs;

import java.io.IOException;
import java.io.PrintWriter;
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

import net.sf.json.JSONObject;

@WebServlet("/bbs/*")
public class BoardServlet extends MyServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		
		String uri=req.getRequestURI();
		
		// ���� ����
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		// AJAX���� �α����� �ȵ� ��� 403�̶�� ���� �ڵ带 ������.
		String header=req.getHeader("AJAX");
		if(header!=null && header.equals("true")  && info==null) {
			resp.sendError(403);
			return;
		}
	
		// AJAX�� �ƴ� ��쿡 �α����� �ȵ� ���
		if(info==null) {
			forward(req, resp, "/WEB-INF/views/member/login.jsp");
			return;
		}
		
		// uri�� ���� �۾� ����
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
		} else if(uri.indexOf("countBoardLike.do")!=-1) {
			// �Խù� ���� ����
			countBoardLike(req, resp);
		} else if(uri.indexOf("insertBoardLike.do")!=-1) {
			// �Խù� ���� ����
			insertBoardLike(req, resp);
		} else if(uri.indexOf("insertReply.do")!=-1) {
			// ��� �߰�
			insertReply(req, resp);
		} else if(uri.indexOf("listReply.do")!=-1) {
			// ��� ����Ʈ
			listReply(req, resp);
		} else if(uri.indexOf("deleteReply.do")!=-1) {
			// ��� ����
			deleteReply(req, resp);
		} else if(uri.indexOf("insertReplyLike.do")!=-1) {
			// ��� ���ƿ�/�Ⱦ�� �߰�
			insertReplyLike(req, resp);
		} else if(uri.indexOf("countReplyLike.do")!=-1) {
			// ��� ���ƿ�/�Ⱦ�� ����
			countReplyLike(req, resp);
		} else if(uri.indexOf("insertReplyAnswer.do")!=-1) {
			// ����� ��� �߰�
			insertReplyAnswer(req, resp);
		} else if(uri.indexOf("listReplyAnswer.do")!=-1) {
			// ����� ��� ����Ʈ
			listReplyAnswer(req, resp);
		} else if(uri.indexOf("deleteReplyAnswer.do")!=-1) {
			// ����� ��� ����
			deleteReplyAnswer(req, resp);
		} else if(uri.indexOf("countReplyAnswer.do")!=-1) {
			// ����� ��� ����
			countReplyAnswer(req, resp);
		}
	}

	private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �Խù� ����Ʈ
		String cp = req.getContextPath();

		BoardDAO dao = new BoardDAO();
		MyUtil util = new MyUtil();
	
		String page=req.getParameter("page");
		int current_page=1;
		if(page!=null)
			current_page=Integer.parseInt(page);
		
		// �˻�
		String searchKey=req.getParameter("searchKey");
		String searchValue=req.getParameter("searchValue");
		if(searchKey==null) {
			searchKey="subject";
			searchValue="";
		}
		// GET ����� ��� ���ڵ�
		if(req.getMethod().equalsIgnoreCase("GET")) {
			searchValue=URLDecoder.decode(searchValue, "utf-8");
		}
		
		// ��ü ������ ����
		int dataCount;
		if(searchValue.length()==0)
			dataCount=dao.dataCount();
		else
			dataCount=dao.dataCount(searchKey, searchValue);
		
		// ��ü ������ ��
		int rows=10;
		int total_page=util.pageCount(rows, dataCount);
		
		if(current_page>total_page)
			current_page=total_page;
		
		// �Խù� ������ ���۰� ��
		int start=(current_page-1)*rows+1;
		int end=current_page*rows;
		
		// �Խù� ��������
		List<BoardDTO> list=null;
		if(searchValue.length()==0)
			list=dao.listBoard(start, end);
		else
			list=dao.listBoard(start, end, searchKey, searchValue);
		
		// ����Ʈ �۹�ȣ �����
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
			// �˻��� ��� �˻��� ���ڵ�
			searchValue=URLEncoder.encode(searchValue, "utf-8");
			query="searchKey="+searchKey+
					 "&searchValue="+searchValue;
		}
		
		// ����¡ ó��
		String listUrl=cp+"/bbs/list.do";
		String articleUrl=cp+"/bbs/article.do?page="+current_page;
		if(query.length()!=0) {
			listUrl+="?"+query;
			articleUrl+="&"+query;
		}
		
		String paging=util.paging(current_page, total_page, listUrl);
		
		// �������� JSP�� �ѱ� �Ӽ�
		req.setAttribute("list", list);
		req.setAttribute("page", current_page);
		req.setAttribute("total_page", total_page);
		req.setAttribute("dataCount", dataCount);
		req.setAttribute("articleUrl", articleUrl);
		req.setAttribute("paging", paging);
		
		// JSP�� ������
		forward(req, resp, "/WEB-INF/views/bbs/list.jsp");
	}
	
	private void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �۾��� ��
		req.setAttribute("mode", "created");

		forward(req, resp, "/WEB-INF/views/bbs/created.jsp");
	}
	
	private void createdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �� ����
		String cp = req.getContextPath();

		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		BoardDAO dao = new BoardDAO();
		BoardDTO dto=new BoardDTO();
		
		// userId�� ���ǿ� ����� ����
		dto.setUserId(info.getUserId());
		
		// �Ķ����
		dto.setSubject(req.getParameter("subject"));
		dto.setContent(req.getParameter("content"));
		
		dao.insertBoard(dto);
		
		resp.sendRedirect(cp+"/bbs/list.do");
	}

	private void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �ۺ���
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
		
		// ��ȸ�� ����
		dao.updateHitCount(num);
		
		// �Խù� ��������
		BoardDTO dto=dao.readBoard(num);
		if(dto==null) { // �Խù��� ������ �ٽ� ����Ʈ��
			resp.sendRedirect(cp+"/bbs/list.do?page="+page);
			return;
		}
		dto.setContent(myUtil.htmlSymbols(dto.getContent()));
		
		// ������ ������
		BoardDTO preReadDto=dao.preReadBoard(dto.getNum(), searchKey, searchValue);
		BoardDTO nextReadDto=dao.nextReadBoard(dto.getNum(), searchKey, searchValue);
		
		// �Խù� ���� ����
		int countBoardLike = dao.countBoardLike(num);
		
		// ����Ʈ�� ������/�����ۿ��� ����� �Ķ����
		String query="page="+page;
		if(searchValue.length()!=0) {
			query+="&searchKey="+searchKey
					+"&searchValue="+URLEncoder.encode(searchValue, "utf-8");
		}
		
		// JSP�� ������ �Ӽ�
		req.setAttribute("dto", dto);
		req.setAttribute("page", page);
		req.setAttribute("query", query);
		req.setAttribute("preReadDto", preReadDto);
		req.setAttribute("nextReadDto", nextReadDto);
		req.setAttribute("countBoardLike", countBoardLike);
		
		// ������
		forward(req, resp, "/WEB-INF/views/bbs/article.jsp");
	}
	
	private void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ���� ��
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
		
		// �Խù��� �ø� ����ڰ� �ƴϸ�
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
		// ���� �Ϸ�
		String cp = req.getContextPath();
		BoardDAO dao = new BoardDAO();
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
	
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
		// ����
		String cp = req.getContextPath();

		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		BoardDAO dao = new BoardDAO();
	
		String page=req.getParameter("page");
		int num=Integer.parseInt(req.getParameter("num"));
		
		// bbsReply ���̺��� ON DELETE CASCADE �ɼ����� bbs ���̺��� �����Ͱ� �������� �ڵ� ������
		dao.deleteBoard(num, info.getUserId());
		resp.sendRedirect(cp+"/bbs/list.do?page="+page);
	}

	private void countBoardLike(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �Խù� ���� ���� - AJAX:JSON

	}
	
	private void insertBoardLike(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �Խù� ���� ���� - AJAX:JSON

	}
	
	private void listReply(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ���� ����Ʈ - AJAX:TEXT
		BoardDAO dao = new BoardDAO();
		MyUtil util = new MyUtil();
		
		int num = Integer.parseInt(req.getParameter("num"));
		int current_page = 1;
		String pageNo = req.getParameter("pageNo");
		
		if(pageNo != null) {
			current_page = Integer.parseInt(pageNo);
		}
		
		int rows = 5;
		int total_page = 0;
		int replyCount = 0;
		
		replyCount = dao.dataCountReply(num);
		total_page = util.pageCount(rows, replyCount);
		
		if(current_page > total_page) {
			current_page = total_page;
		}
		
		int start = (current_page - 1) * rows + 1;
		int end = current_page * rows;
		
		List<ReplyDTO> list = dao.listReply(num, start, end);
		
		for(ReplyDTO dto : list) {
			dto.setContent(util.htmlSymbols(dto.getContent()));
		}
		
		String paging = util.pagingMethod(current_page, total_page, "listPage");
		
		req.setAttribute("list", list);
		req.setAttribute("pageNo", current_page);
		req.setAttribute("replyCount", replyCount);
		req.setAttribute("total_page", total_page);
		req.setAttribute("paging", paging);
		
		forward(req, resp, "/WEB-INF/views/bbs/listReply.jsp");
	}

	private void insertReply(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ���� �Ǵ� ���  ���� - AJAX:JSON
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		BoardDAO dao = new BoardDAO();
		ReplyDTO dto = new ReplyDTO();
		
		dto.setUserId(info.getUserId());
		dto.setNum(Integer.parseInt(req.getParameter("num")));
		dto.setAnswer(Integer.parseInt(req.getParameter("answer")));
		dto.setContent(URLDecoder.decode(req.getParameter("content"), "utf-8"));
		
		dao.insertReply(dto);
		
		String state = "true";
		JSONObject job = new JSONObject();
		job.put("state", state);
		
		resp.setContentType("text/html;charset=utf-8");
		PrintWriter out = resp.getWriter();
		out.print(job.toString());
	}

	private void deleteReply(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ���� �Ǵ� ��� ���� - AJAX:JSON

	}

	private void insertReplyLike(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ��� ���ƿ� / �Ⱦ�� ���� - AJAX:JSON

	}

	private void countReplyLike(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ��� ���ƿ� / �Ⱦ�� ���� - AJAX:JSON

	}

	private void insertReplyAnswer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ��� ���� - AJAX:JSON

	}

	private void listReplyAnswer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ������ ��� ����Ʈ - AJAX:TEXT

	}

	private void deleteReplyAnswer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ���� ��� ���� - AJAX:JSON

	}
	
	private void countReplyAnswer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ������ ��� ���� - AJAX:JSON

	}
}
