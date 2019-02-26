package com.member;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.util.MyServlet;

@WebServlet("/member/*")
public class MemberServlet extends MyServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		
		String uri=req.getRequestURI();
		
		// uri에 따른 작업 구분
		if(uri.indexOf("login.do")!=-1) {
			loginForm(req, resp);
		} else if(uri.indexOf("login_ok.do")!=-1) {
			loginSubmit(req, resp);
		} else if(uri.indexOf("logout.do")!=-1) {
			logout(req, resp);
		} else if(uri.indexOf("member.do")!=-1) {
			memberForm(req, resp);
		} else if(uri.indexOf("member_ok.do")!=-1) {
			memberSubmit(req, resp);
		} else if(uri.indexOf("pwd.do")!=-1) {
			pwdForm(req, resp);
		} else if(uri.indexOf("pwd_ok.do")!=-1) {
			pwdSubmit(req, resp);
		} else if(uri.indexOf("update_ok.do")!=-1) {
			updateSubmit(req, resp);
		} else if(uri.indexOf("userIdCheck.do")!=-1) {
			userIdCheck(req, resp);
		}
	}

	private void loginForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 로그인 폼
		String path="/WEB-INF/views/member/login.jsp";
		forward(req, resp, path);
	}

	private void loginSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 로그인 처리
		// 세션객체. 세션 정보는 서버에 저장(로그인정보, 권한등을저장)
		HttpSession session=req.getSession();
		
		MemberDAO dao=new MemberDAO();
		String cp=req.getContextPath();
		
		String userId=req.getParameter("userId");
		String userPwd=req.getParameter("userPwd");
		
		MemberDTO dto=dao.readMember(userId);
		if(dto!=null) {
			if(userPwd.equals(dto.getUserPwd()) && dto.getEnabled()==1) {
				// 로그인 성공 : 로그인정보를 서버에 저장
				// 세션의 유지시간을 20분설정(기본 30분)
				session.setMaxInactiveInterval(20*60);
				
				// 세션에 저장할 내용
				SessionInfo info=new SessionInfo();
				info.setUserId(dto.getUserId());
				info.setUserName(dto.getUserName());
				
				// 세션에 member이라는 이름으로 저장
				session.setAttribute("member", info);
				
				// 메인화면으로 리다이렉트
				resp.sendRedirect(cp);
				return;
			} 
		}
		
		// 로그인 실패인 경우(다시 로그인 폼으로)
		String msg="아이디 또는 패스워드가 일치하지 않습니다.";
		req.setAttribute("message", msg);
		
		forward(req, resp, "/WEB-INF/views/member/login.jsp");
	}
	
	private void logout(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 로그아웃
		HttpSession session=req.getSession();
		String cp=req.getContextPath();

		// 세션에 저장된 정보를 지운다.
		session.removeAttribute("member");
		
		// 세션에 저장된 모든 정보를 지우고 세션을 초기화 한다.
		session.invalidate();
		
		// 루트로 리다이렉트
		resp.sendRedirect(cp);
	}
	
	private void memberForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 회원가입폼
		req.setAttribute("title", "회원 가입");
		req.setAttribute("mode", "created");
		
		forward(req, resp, "/WEB-INF/views/member/member.jsp");
	}

	private void memberSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 회원가입 처리
		MemberDAO dao=new MemberDAO();
		MemberDTO dto = new MemberDTO();
		
		dto.setUserId(req.getParameter("userId"));
		dto.setUserPwd(req.getParameter("userPwd"));
		dto.setUserName(req.getParameter("userName"));
		dto.setBirth(req.getParameter("birth"));
		String email1 = req.getParameter("email1");
		String email2 = req.getParameter("email2");
		if (email1 != null && email1.length() != 0 && email2 != null
				&& email2.length() != 0) {
			dto.setEmail(email1 + "@" + email2);
		}
		String tel1 = req.getParameter("tel1");
		String tel2 = req.getParameter("tel2");
		String tel3 = req.getParameter("tel3");
		if (tel1 != null && tel1.length() != 0 && tel2 != null
				&& tel2.length() != 0 && tel3 != null && tel3.length() != 0) {
			dto.setTel(tel1 + "-" + tel2 + "-" + tel3);
		}
		dto.setZip(req.getParameter("zip"));
		dto.setAddr1(req.getParameter("addr1"));
		dto.setAddr2(req.getParameter("addr2"));

		int result = dao.insertMember(dto);
		if (result == 0) {
			String message = "회원 가입이 실패 했습니다.";

			req.setAttribute("title", "회원 가입");
			req.setAttribute("mode", "created");
			req.setAttribute("message", message);
			forward(req, resp, "/WEB-INF/views/member/member.jsp");
			return;
		}

		StringBuffer sb=new StringBuffer();
		sb.append("<b>"+dto.getUserName()+ "</b>님 회원가입이 되었습니다.<br>");
		sb.append("메인화면으로 이동하여 로그인 하시기 바랍니다.<br>");
		
		req.setAttribute("title", "회원 가입");
		req.setAttribute("message", sb.toString());
		
		forward(req, resp, "/WEB-INF/views/member/complete.jsp");
	}
	
	private void pwdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 패스워드 확인 폼
		HttpSession session=req.getSession();
		String cp=req.getContextPath();
		
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		if(info==null) {
			// 로그아웃상태이면
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		String mode=req.getParameter("mode");
		if(mode.equals("update"))
			req.setAttribute("title", "회원 정보 수정");
		else
			req.setAttribute("title", "회원 탈퇴");
		
		req.setAttribute("mode", mode);
		
		forward(req, resp, "/WEB-INF/views/member/pwd.jsp");
	}

	private void pwdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 패스워드 확인
		HttpSession session=req.getSession();
		String cp=req.getContextPath();
		MemberDAO dao=new MemberDAO();
		
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		if(info==null) { //로그아웃 된 경우
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		// DB에서 해당 회원 정보 가져오기
		MemberDTO dto=dao.readMember(info.getUserId());
		if(dto==null) {
			session.invalidate();
			resp.sendRedirect(cp);
			return;
		}
		
		String userPwd=req.getParameter("userPwd");
		String mode=req.getParameter("mode");
		if(! dto.getUserPwd().equals(userPwd)) {
			if(mode.equals("update"))
				req.setAttribute("title", "회원 정보 수정");
			else
				req.setAttribute("title", "회원 탈퇴");
	
			req.setAttribute("mode", mode);
			req.setAttribute("message", 	"<span style='color:red;'>패스워드가 일치하지 않습니다.</span>");
			forward(req, resp, "/WEB-INF/views/member/pwd.jsp");
			return;
		}
		
		if(mode.equals("delete")) {
			// 회원탈퇴
			dao.deleteMember(info.getUserId());
			
			session.removeAttribute("member");
			session.invalidate();
			
			StringBuffer sb=new StringBuffer();
			sb.append("<b>"+dto.getUserName()+ "</b>님 회원탈퇴가 정상처리되었습니다.<br>");
			sb.append("그동안 이용해 주셔서 감사합니다.<br>");
			
			req.setAttribute("title", "회원 탈퇴");
			req.setAttribute("message", sb.toString());
			
			forward(req, resp, "/WEB-INF/views/member/complete.jsp");
			
			return;
		}
		
		// 회원정보수정
		if(dto.getEmail()!=null) {
			String []s=dto.getEmail().split("@");
			if(s.length==2) {
				dto.setEmail1(s[0]);
				dto.setEmail2(s[1]);
			}
		}
		
		if(dto.getTel()!=null) {
			String []s=dto.getTel().split("-");
			if(s.length==3) {
				dto.setTel1(s[0]);
				dto.setTel2(s[1]);
				dto.setTel3(s[2]);
			}
		}
		
		// 회원수정폼으로 이동
		req.setAttribute("title", "회원 정보 수정");
		req.setAttribute("dto", dto);
		req.setAttribute("mode", "update");
		forward(req, resp, "/WEB-INF/views/member/member.jsp");
	}

	private void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 회원정보 수정 완료
		HttpSession session=req.getSession();
		String cp=req.getContextPath();
		MemberDAO dao=new MemberDAO();
		
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		if(info==null) { //로그아웃 된 경우
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		MemberDTO dto = new MemberDTO();

		dto.setUserId(req.getParameter("userId"));
		dto.setUserPwd(req.getParameter("userPwd"));
		dto.setUserName(req.getParameter("userName"));
		dto.setBirth(req.getParameter("birth"));
		String email1 = req.getParameter("email1");
		String email2 = req.getParameter("email2");
		if (email1 != null && email1.length() != 0 && email2 != null
				&& email2.length() != 0) {
			dto.setEmail(email1 + "@" + email2);
		}
		String tel1 = req.getParameter("tel1");
		String tel2 = req.getParameter("tel2");
		String tel3 = req.getParameter("tel3");
		if (tel1 != null && tel1.length() != 0 && tel2 != null
				&& tel2.length() != 0 && tel3 != null && tel3.length() != 0) {
			dto.setTel(tel1 + "-" + tel2 + "-" + tel3);
		}
		dto.setZip(req.getParameter("zip"));
		dto.setAddr1(req.getParameter("addr1"));
		dto.setAddr2(req.getParameter("addr2"));
		
		dao.updateMember(dto);
		
		StringBuffer sb=new StringBuffer();
		sb.append("<b>"+dto.getUserName()+ "</b>님 회원 정보가 수정 되었습니다.<br>");
		sb.append("메인화면으로 이동 하시기 바랍니다.<br>");
		
		req.setAttribute("title", "회원 정보 수정");
		req.setAttribute("message", sb.toString());
		
		forward(req, resp, "/WEB-INF/views/member/complete.jsp");
	}
	
	private void userIdCheck(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 아이디 중복 검사

	}	
}
