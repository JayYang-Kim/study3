package com.notice;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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

@WebServlet("/notice/*")
public class NoticeServlet extends MyServlet {
	private static final long serialVersionUID = 1L;

	private String pathname;
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		String uri=req.getRequestURI();
		
		HttpSession session=req.getSession();
		
		// 파일을 저장할 경로(pathname)
		String root=session.getServletContext().getRealPath("/");
		pathname=root+File.separator+"uploads"+File.separator+"notice";
		File f=new File(pathname);
		if(! f.exists()) {
			f.mkdirs();
		}
		
		// uri에 따른 작업 구분
		if(uri.indexOf("list.do")!=-1) {
			list(req, resp);
		} else if(uri.indexOf("created.do")!=-1) {
			createdForm(req, resp);
		}  else if(uri.indexOf("created_ok.do")!=-1) {
			createdSubmit(req, resp);
		}  else if(uri.indexOf("article.do")!=-1) {
			article(req, resp);
		} else if(uri.indexOf("update.do")!=-1) {
			updateForm(req, resp);
		} else if(uri.indexOf("update_ok.do")!=-1) {
			updateSubmit(req, resp);
		} else if(uri.indexOf("deleteFile.do")!=-1) {
			deleteFile(req, resp);
		} else if(uri.indexOf("delete.do")!=-1) {
			delete(req, resp);
		} else if(uri.indexOf("download.do")!=-1) {
			download(req, resp);
		}
	}

	private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 게시물 리스트
		NoticeDAO dao=new NoticeDAO();
		MyUtil util=new MyUtil();		
		String cp=req.getContextPath();
		
		String page=req.getParameter("page");
		int current_page=1;
		if(page!=null)
			current_page=Integer.parseInt(page);
		
		String searchKey=req.getParameter("searchKey");
		String searchValue=req.getParameter("searchValue");
		if(searchKey==null) {
			searchKey="subject";
			searchValue="";
		}
		if(req.getMethod().equalsIgnoreCase("GET")) {
			searchValue=URLDecoder.decode(searchValue,"utf-8");
		}
		
		int rows=10;
		int dataCount, total_page;
		
		if(searchValue.length()!=0)
			dataCount= dao.dataCount(searchKey, searchValue);
		else
			dataCount= dao.dataCount();
		total_page=util.pageCount(rows, dataCount);
		
		if(current_page>total_page)
			current_page=total_page;
		
		int start=(current_page-1)*rows+1;
		int end=current_page*rows;
		
		List<NoticeDTO> list;
		if(searchValue.length()!=0)
			list= dao.listNotice(start, end, searchKey, searchValue);
		else
			list= dao.listNotice(start, end);
		
		// 공지글
		List<NoticeDTO> listNotice=null;
		listNotice = dao.listNotice();
		Iterator<NoticeDTO> itNotice=listNotice.iterator();
		while (itNotice.hasNext()) {
			NoticeDTO dto=itNotice.next();
			dto.setCreated(dto.getCreated().substring(0, 10));
		}
		
        Date endDate = new Date();
        long gap;
		// 리스트 글번호 만들기
		int listNum, n=0;
		Iterator<NoticeDTO> it=list.iterator();
		while (it.hasNext()) {
			NoticeDTO dto=it.next();
			listNum=dataCount-(start+n-1);
			dto.setListNum(listNum);
			
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date beginDate = formatter.parse(dto.getCreated());
/*            
            	// 날짜차이(일)
            	gap=(endDate.getTime() - beginDate.getTime()) / (24 * 60 * 60* 1000);
            	dto.setGap(gap);
*/
				// 날짜차이(시간)
				gap=(endDate.getTime() - beginDate.getTime()) / (60*60* 1000);
				dto.setGap(gap);
			}catch(Exception e) {
			}
            
            dto.setCreated(dto.getCreated().substring(0, 10));
			
			n++;
		}
		
		String query="";
		String listUrl;
		String articleUrl;
		
		if(searchValue.length()==0) {
			listUrl=cp+"/notice/list.do";
			articleUrl=cp+"/notice/article.do?page=" +current_page;
		} else {
			query="searchKey="+searchKey;
			query+="&searchValue="+URLEncoder.encode(searchValue,"utf-8");
			
			listUrl=cp+"/notice/list.do?"+query;
			articleUrl=cp+"/notice/article.do?page="+current_page+"&"+query;
		}
		
		String paging=util.paging(current_page, total_page, listUrl);
		
		// 포워딩 jsp에 넘길 데이터
		req.setAttribute("list", list);
		req.setAttribute("listNotice", listNotice);
		req.setAttribute("articleUrl", articleUrl);
		req.setAttribute("dataCount", dataCount);
		req.setAttribute("page", current_page);
		req.setAttribute("total_page", total_page);
		req.setAttribute("paging", paging);
		
		// JSP로 포워딩
		forward(req, resp, "/WEB-INF/views/notice/list.jsp");
	}
	
	private void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글쓰기 폼
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		String cp=req.getContextPath();
		
		if(info==null) {
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		// admin만 글을 등록
		if(! info.getUserId().equals("admin")) {
			resp.sendRedirect(cp+"/notice/list.do");
			return;
		}
		
		req.setAttribute("mode", "created");

		forward(req, resp, "/WEB-INF/views/notice/created.jsp");
	}
	
	private void createdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 저장
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		NoticeDAO dao=new NoticeDAO();
		String cp=req.getContextPath();
		
		if(info==null) {
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		// admin만 글을 등록
		if(! info.getUserId().equals("admin")) {
			resp.sendRedirect(cp+"/notice/list.do");
			return;
		}
		
		String encType="utf-8";
		int maxFilesize=10*1024*1024;
		
	    MultipartRequest mreq=new MultipartRequest(
	    		req, pathname, maxFilesize, encType,
	    		new DefaultFileRenamePolicy()
	    		);
		
	    NoticeDTO dto=new NoticeDTO();
	    dto.setUserId(info.getUserId());
	    if(mreq.getParameter("notice")!=null)
	    	dto.setNotice(Integer.parseInt(mreq.getParameter("notice")));
	    dto.setSubject(mreq.getParameter("subject"));
	    dto.setContent(mreq.getParameter("content"));
	    
	    if(mreq.getFile("upload")!=null) {
	    	dto.setSaveFilename(mreq.getFilesystemName("upload"));
	    	dto.setOriginalFilename(mreq.getOriginalFileName("upload"));
		    dto.setFileSize(mreq.getFile("upload").length());
		}
	    dao.insertNotice(dto);
		
		resp.sendRedirect(cp+"/notice/list.do");
	}

	private void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글보기
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		NoticeDAO dao=new NoticeDAO();
		String cp=req.getContextPath();
		
		if(info==null) {
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		int num=Integer.parseInt(req.getParameter("num"));
		String page=req.getParameter("page");
		
		String searchKey=req.getParameter("searchKey");
		String searchValue=req.getParameter("searchValue");
		if(searchKey==null) {
			searchKey="subject";
			searchValue="";
		}
		searchValue=URLDecoder.decode(searchValue, "utf-8");
		
		// 조회수
		dao.updateHitCount(num);
		
		// 게시물 가져오기
		NoticeDTO dto=dao.readNotice(num);
		if(dto==null) {
			resp.sendRedirect(cp+"/notice/list.do?page="+page);
			return;
		}
		
		dto.setContent(dto.getContent().replaceAll("\n", "<br>"));
		
		// 이전글/다음글
		NoticeDTO preReadDto = dao.preReadNotice(dto.getNum(), searchKey, searchValue);
		NoticeDTO nextReadDto = dao.nextReadNotice(dto.getNum(), searchKey, searchValue);
		
		String query="page="+page;
		if(searchValue.length()!=0) {
			query+="&searchKey="+searchKey;
			query+="&searchValue="+URLEncoder.encode(searchValue, "utf-8");
		}
		
		req.setAttribute("dto", dto);
		req.setAttribute("preReadDto", preReadDto);
		req.setAttribute("nextReadDto", nextReadDto);
		req.setAttribute("query", query);
		req.setAttribute("page", page);
		
		forward(req, resp, "/WEB-INF/views/notice/article.jsp");
	}
	
	private void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 수정 폼
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		NoticeDAO dao=new NoticeDAO();
		String cp=req.getContextPath();
		
		if(info==null) {
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}

		String page=req.getParameter("page");
		int num=Integer.parseInt(req.getParameter("num"));
		
		NoticeDTO dto=dao.readNotice(num);
		if(dto==null) {
			resp.sendRedirect(cp+"/notice/list.do?page="+page);
			return;
		}
		
		// 글을 등록한 사람만 수정 가능
		if(! info.getUserId().equals(dto.getUserId())) {
			resp.sendRedirect(cp+"/notice/list.do?page="+page);
			return;
		}
		
		req.setAttribute("dto", dto);
		req.setAttribute("page", page);
		
		req.setAttribute("mode", "update");

		forward(req, resp, "/WEB-INF/views/notice/created.jsp");
	}

	private void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 수정 완료
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");

		NoticeDAO dao=new NoticeDAO();
		String cp=req.getContextPath();
		
		if(info==null) {
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		String encType="utf-8";
		int maxFilesize=10*1024*1024;
		
	    MultipartRequest mreq=new MultipartRequest(
	    		req, pathname, maxFilesize, encType,
	    		new DefaultFileRenamePolicy()
	    		);
		
		NoticeDTO dto=new NoticeDTO();
		
		int num=Integer.parseInt(mreq.getParameter("num"));
		String page=mreq.getParameter("page");
		
		dto.setNum(num);
	    if(mreq.getParameter("notice")!=null)
	    	dto.setNotice(Integer.parseInt(mreq.getParameter("notice")));
		dto.setSubject(mreq.getParameter("subject"));
		dto.setContent(mreq.getParameter("content"));
		dto.setSaveFilename(mreq.getParameter("saveFilename"));
		dto.setOriginalFilename(mreq.getParameter("originalFilename"));
		dto.setFileSize(Long.parseLong(mreq.getParameter("fileSize")));

		if(mreq.getFile("upload")!=null) {
			// 기존 파일 삭제
			FileManager.doFiledelete(pathname, mreq.getParameter("saveFilename"));
			
	    	dto.setSaveFilename(mreq.getFilesystemName("upload"));
	    	dto.setOriginalFilename(mreq.getOriginalFileName("upload"));
		    dto.setFileSize(mreq.getFile("upload").length());
		}
		
		dao.updateNotice(dto);
		
		resp.sendRedirect(cp+"/notice/list.do?page="+page);
	}

	private void deleteFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 파일 삭제
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		NoticeDAO dao=new NoticeDAO();
		String cp=req.getContextPath();
	
		int num=Integer.parseInt(req.getParameter("num"));
		String page=req.getParameter("page");
		
		NoticeDTO dto=dao.readNotice(num);
		if(dto==null) {
			resp.sendRedirect(cp+"/notice/list.do?page="+page);
			return;
		}
		
		if(info==null || ! info.getUserId().equals(dto.getUserId())) {
			resp.sendRedirect(cp+"/notice/list.do?page="+page);
			return;
		}
		
		FileManager.doFiledelete(pathname, dto.getSaveFilename());
		dto.setOriginalFilename("");
		dto.setSaveFilename("");
		dto.setFileSize(0);
		
		dao.updateNotice(dto);
		
		req.setAttribute("dto", dto);
		req.setAttribute("page", page);
		
		req.setAttribute("mode", "update");

		forward(req, resp, "/WEB-INF/views/notice/created.jsp");			
	}

	private void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 삭제
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		NoticeDAO dao=new NoticeDAO();
		String cp=req.getContextPath();
		
		if(info==null) {
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		int num=Integer.parseInt(req.getParameter("num"));
		String page=req.getParameter("page");
		
		NoticeDTO dto=dao.readNotice(num);
		if(dto==null) {
			resp.sendRedirect(cp+"/notice/list.do?page="+page);
			return;
		}
		
		// 글을 등록한 사람, admin 만 삭제 가능
		if(! info.getUserId().equals(dto.getUserId()) && ! info.getUserId().equals("admin")) {
			resp.sendRedirect(cp+"/notice/list.do?page="+page);
			return;
		}
		
		if(dto.getSaveFilename()!=null && dto.getSaveFilename().length()!=0)
		   FileManager.doFiledelete(pathname, dto.getSaveFilename());
		
		dao.deleteNotice(num);
		
		resp.sendRedirect(cp+"/notice/list.do?page="+page);
	}

	private void download(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 파일 다운로드
		HttpSession session=req.getSession();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		NoticeDAO dao=new NoticeDAO();
		String cp=req.getContextPath();
		
		if(info==null) {
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		int num=Integer.parseInt(req.getParameter("num"));
		String page=req.getParameter("page");
		
		NoticeDTO dto=dao.readNotice(num);
		if(dto==null) {
			resp.sendRedirect(cp+"/notice/list.do"+page);
			return;
		}
		
		boolean b = FileManager.doFiledownload(dto.getSaveFilename(),
				dto.getOriginalFilename(), pathname, resp);
		
		if(! b) {
			resp.setContentType("text/html;charset=utf-8");
	    	PrintWriter pw=resp.getWriter();
	    	pw.print("<script>alert('파일다운로드가 실패했습니다. !!!');history.back();</script>");
	    	return;
		}
	}
}
