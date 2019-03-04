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
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
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
		if(info==null) { // 로그인되지 않은 경우
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		// 이미지를 저장할 경로
		String root = session.getServletContext().getRealPath("/");
		pathname = root + "uploads" + File.separator + "photo";
		
		File f = new File(pathname);
		
		if(!f.exists()) {
			f.mkdirs();
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
		String cp=req.getContextPath();
		PhotoDAO dao=new PhotoDAO();
		MyUtil util=new MyUtil();
		
		String page=req.getParameter("page");
		int current_page=1;
		if(page!=null)
			current_page=Integer.parseInt(page);
		
		// 전체데이터 개수
		int dataCount=dao.dataCount();

		// 전체페이지수
		int rows=6;
		int total_page=util.pageCount(rows, dataCount);
		if(current_page>total_page)
			current_page=total_page;
		
		// 게시물 가져올 시작과 끝위치
		int start=(current_page-1)*rows+1;
		int end=current_page*rows;
		
		// 게시물 가져오기
		List<PhotoDTO> list=dao.listPhoto(start, end);
		
		// 페이징 처리
		String listUrl=cp+"/photo/list.do";
		String articleUrl = cp + "/photo/article.do?page="+current_page;
		String paging=util.paging(current_page, total_page, listUrl);
		
		// 포워딩할 list.jsp에 넘길 값
		req.setAttribute("list", list);
		req.setAttribute("dataCount", dataCount);
		req.setAttribute("articleUrl", articleUrl);
		req.setAttribute("page", current_page);
		req.setAttribute("total_page", total_page);
		req.setAttribute("paging", paging);
		
		forward(req, resp, "/WEB-INF/views/photo/list.jsp");
	}
	
	private void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글쓰기 폼
		req.setAttribute("mode", "created");

		forward(req, resp, "/WEB-INF/views/photo/created.jsp");
	}
	
	private void createdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 게시물 저장
		String cp = req.getContextPath();
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		PhotoDAO dao = new PhotoDAO();
		
		String encType = "UTF-8";
		int maxSize = 5 * 1024 * 1024;
		
		// request, 서버에 저장할 경로, 최대크기, 파라미터 타입, 동일 파일명 보호
		MultipartRequest mreq = new MultipartRequest(req, pathname, maxSize, encType, new DefaultFileRenamePolicy());
		
		PhotoDTO dto = new PhotoDTO();
		if(mreq.getFile("upload") != null) {
			dto.setUserId(info.getUserId());
			dto.setSubject(mreq.getParameter("subject"));
			dto.setContent(mreq.getParameter("content"));
			
			// 서버에 저장된 파일명
			String saveFilename = mreq.getFilesystemName("upload");
			
			// 파일명 변경
			saveFilename = FileManager.doFilerename(pathname, saveFilename);
			dto.setImageFilename(saveFilename);
			
			dao.insertPhoto(dto);
		}
		
		resp.sendRedirect(cp + "/photo/list.do");
	}

	private void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 게시물 보기
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
		// 수정 폼
		String cp = req.getContextPath();
		
		PhotoDAO dao = new PhotoDAO();
		
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		int num = Integer.parseInt(req.getParameter("num"));
		String page = req.getParameter("page");
		
		// DB에서 해당 게시물 가져오기
		PhotoDTO dto = dao.readPhoto(num);
		
		if(dto == null || !dto.getUserId().equals(info.getUserId())) { // 게시물이 없거나, 작성자와 로그인 아이디가 다른경우
			resp.sendRedirect(cp + "/photo/list.do?page=" + page);
			return;
		}
		
		req.setAttribute("mode", "update");
		req.setAttribute("dto", dto);
		req.setAttribute("page", page);

		forward(req, resp, "/WEB-INF/views/photo/created.jsp");
	}

	private void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 수정 완료
		String cp = req.getContextPath();
		
		PhotoDAO dao = new PhotoDAO();
		
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		String encType = "UTF-8";
		int maxSize = 5 * 1024 * 1024;
		
		// request, 서버에 저장할 경로, 최대크기, 파라미터 타입, 동일 파일명 보호
		MultipartRequest mreq = new MultipartRequest(req, pathname, maxSize, encType, new DefaultFileRenamePolicy());
		
		String page = mreq.getParameter("page");
		if(!mreq.getParameter("userId").equals(info.getUserId())) {
			resp.sendRedirect(cp + "/photo/list.do?page=" + page);
			return;
		}
		
		PhotoDTO dto = new PhotoDTO();
		
		dto.setNum(Integer.parseInt(mreq.getParameter("num")));
		dto.setSubject(mreq.getParameter("subject"));
		dto.setContent(mreq.getParameter("content"));
		dto.setImageFilename(mreq.getParameter("imageFilename"));
		
		if(mreq.getFile("upload") != null) { // 수정한 파일이 있는경우
			// 기존 파일 지우기
			FileManager.doFiledelete(pathname, dto.getImageFilename());
			
			// 서버에 저장된 새로운 파일명
			String saveFilename = mreq.getFilesystemName("upload");
			
			// 이름 변경
			saveFilename = FileManager.doFilerename(pathname, saveFilename);
			
			dto.setImageFilename(saveFilename);
		}
		
		dao.updatePhoto(dto);
		
		resp.sendRedirect(cp + "/photo/article.do?num=" + dto.getNum() + "&page=" + page);
	}

	private void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 삭제 완료
		String cp = req.getContextPath();
		PhotoDAO dao = new PhotoDAO();
		
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		int num = Integer.parseInt(req.getParameter("num"));
		String page = req.getParameter("page");
		
		// DB에서 해당 게시물 가져오기
		PhotoDTO dto = dao.readPhoto(num);
		if(dto == null || (!dto.getUserId().equals(info.getUserId()) && !info.getUserId().equals("admin"))) {
			resp.sendRedirect(cp + "/photo/list.do?page=" + page);
			return;
		}
		
		// 게시물 삭제 전 이미지 삭제
		FileManager.doFiledelete(pathname, dto.getImageFilename());
		
		dao.deletePhoto(num);
		
		resp.sendRedirect(cp + "/photo/list.do?page=" + page);
	}
}
