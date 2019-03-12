<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%
   String cp = request.getContextPath();
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>spring</title>

<link rel="stylesheet" href="<%=cp%>/resource/css/style.css" type="text/css">
<link rel="stylesheet" href="<%=cp%>/resource/css/layout.css" type="text/css">
<link rel="stylesheet" href="<%=cp%>/resource/jquery/css/smoothness/jquery-ui.min.css" type="text/css">
<style type="text/css">
.guest-write {
    border: #d5d5d5 solid 1px;
    padding: 10px;
    min-height: 50px;
}
</style>
<script type="text/javascript" src="<%=cp%>/resource/js/util.js"></script>
<script type="text/javascript" src="<%=cp%>/resource/jquery/js/jquery-1.12.4.min.js"></script>
<script type="text/javascript">
function sendGuest() {
	var uid="${sessionScope.member.userId}";
	if(! uid) {
		location.href="<%=cp%>/member/login.do";
		return;
	}
	
	var f=document.guestForm;
	var str;
	
	str=f.content.value;
	if(!str) {
		alert("내용을 입력 하세요 !!!");
		f.content.focus();
		return;
	}
	
	f.action="<%=cp%>/guest/guest_ok.do";
	f.submit();
}

function deleteGuest(num) {
	var url="<%=cp%>/guest/delete.do?num="+num+"&page=${page}";
	
	if(confirm("삭제 하시겠습니까 ?"))
		location.href=url;
}
</script>
</head>
<body>

<div class="header">
    <jsp:include page="/WEB-INF/views/layout/header.jsp"></jsp:include>
</div>
	
<div class="container">
    <div class="body-container" style="width: 700px;">
        <div class="body-title">
            <h3><span style="font-family: Webdings">2</span> 방명록 </h3>
        </div>
        
        <div>
            
             <form name="guestForm" method="post" action="">
             <div class="guest-write">
                 <div style="clear: both;">
                         <span style="font-weight: bold;">방명록쓰기</span><span> - 타인을 비방하거나 개인정보를 유출하는 글의 게시를 삼가 주세요.</span>
                 </div>
                 <div style="clear: both; padding-top: 10px;">
                       <textarea name="content" id="content" class="boxTF" rows="3" style="display:block; width: 100%; padding: 6px 12px; box-sizing:border-box;" required="required"></textarea>
                  </div>
                  <div style="text-align: right; padding-top: 10px;">
                       <button type="button" class="btn" onclick="sendGuest();" style="padding:8px 25px;"> 등록하기 </button>
                  </div>           
            </div>
           </form>
         
           <div id="listGuest" style="width:100%; margin: 0px auto;">
             <c:if test="${dataCount != 0}">
                 <table style='width: 100%; margin: 10px auto 0px; border-spacing: 0px; border-collapse: collapse;'>
                    <tr height='35'>
                        <td width='50%'>
                            <span style='color: #3EA9CD; font-weight: 700;'>방명록 ${dataCount}개</span>
                            <span>[목록, ${page}/${total_page} 페이지]</span>
                        </td>
                        <td width='50%'>
                            &nbsp;
                        </td>
                    </tr>
                    
                    <c:forEach var="dto" items="${list}">
                         <tr height='35' bgcolor='#eeeeee'>
                               <td width='50%' style='padding-left: 5px; border:1px solid #cccccc; border-right:none;'>
                                       작성자 : ${dto.userName}
                                </td>
                                <td width='50%' align='right' style='padding-right: 5px; border:1px solid #cccccc; border-left:none;'>
                                       ${dto.created}
                                       <c:if test="${sessionScope.member.userId==dto.userId || sessionScope.member.userId=='admin'}">    
                                           | <a href="javascript:deleteGuest('${dto.num}');">삭제</a>
                                        </c:if>
                                    </td>
                         </tr>
                          
                         <tr height='50'><td colspan='2' style='padding: 5px;' valign='top'>${dto.content}</td></tr>
                    </c:forEach>  
                          
                         <tr><td colspan='2' height='30' align='center'>${paging}</td></tr>
                 </table>
             </c:if>
           </div>
                    
        </div>
    </div>
</div>

<div class="footer">
    <jsp:include page="/WEB-INF/views/layout/footer.jsp"></jsp:include>
</div>

<script type="text/javascript" src="<%=cp%>/resource/jquery/js/jquery-ui.min.js"></script>
<script type="text/javascript" src="<%=cp%>/resource/jquery/js/jquery.ui.datepicker-ko.js"></script>
</body>
</html>