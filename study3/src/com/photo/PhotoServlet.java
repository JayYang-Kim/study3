package com.photo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.member.SessionInfo;
import com.util.FileManager;
import com.util.MyServlet;
import com.util.MyUtil;

@WebServlet("/photo/*")
public class PhotoServlet extends MyServlet {
	private static final long serialVersionUID = 1L;
	
	private String pathname;

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		String uri=req.getRequestURI();
		
		String cp=req.getContextPath();
		
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		if(info==null) { // �α��ε��� ���� ���
			resp.sendRedirect(cp+"/member/login.do");
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
		}
	}

	private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �Խù� ����Ʈ
		String cp=req.getContextPath();
		PhotoDAO dao=new PhotoDAO();
		MyUtil util=new MyUtil();
		
		String page=req.getParameter("page");
		int current_page=1;
		if(page!=null)
			current_page=Integer.parseInt(page);
		
		// ��ü������ ����
		int dataCount=dao.dataCount();

		// ��ü��������
		int rows=6;
		int total_page=util.pageCount(rows, dataCount);
		if(current_page>total_page)
			current_page=total_page;
		
		// �Խù� ������ ���۰� ����ġ
		int start=(current_page-1)*rows+1;
		int end=current_page*rows;
		
		// �Խù� ��������
		List<PhotoDTO> list=dao.listPhoto(start, end);
		
		// ����¡ ó��
		String listUrl=cp+"/photo/list.do";
		String articleUrl = cp + "/photo/article.do?page="+current_page;
		String paging=util.paging(current_page, total_page, listUrl);
		
		// �������� list.jsp�� �ѱ� ��
		req.setAttribute("list", list);
		req.setAttribute("dataCount", dataCount);
		req.setAttribute("articleUrl", articleUrl);
		req.setAttribute("page", current_page);
		req.setAttribute("total_page", total_page);
		req.setAttribute("paging", paging);
		
		forward(req, resp, "/WEB-INF/views/photo/list.jsp");
	}
	
	private void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �۾��� ��
		req.setAttribute("mode", "created");

		forward(req, resp, "/WEB-INF/views/photo/created.jsp");
	}
	
	private void createdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �Խù� ����
	}

	private void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �Խù� ����
		String cp=req.getContextPath();
		
		PhotoDAO dao=new PhotoDAO();
		
		int num=Integer.parseInt(req.getParameter("num"));
		String page=req.getParameter("page");
		
		PhotoDTO dto=dao.readPhoto(num);
		if(dto==null) {
			resp.sendRedirect(cp+"/photo/list.do?page="+page);
			return;
		}
		
		dto.setContent(dto.getContent().replaceAll("\n", "<br>"));
		
		req.setAttribute("dto", dto);
		req.setAttribute("page", page);
		
		forward(req, resp, "/WEB-INF/views/photo/article.jsp");
	}
	
	private void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ���� ��
		
		req.setAttribute("mode", "update");

		forward(req, resp, "/WEB-INF/views/photo/created.jsp");
	}

	private void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ���� �Ϸ�
		
	}

	private void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ���� �Ϸ�
				
	}
}
