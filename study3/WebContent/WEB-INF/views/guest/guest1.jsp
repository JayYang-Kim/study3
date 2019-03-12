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

$(function(){
	listPage(1);
});

function listPage(page) {
	var query = "pageNo=" + page;
	var url = "<%=cp%>/guest/list.do";
	
	$.ajax({
		type : "post",
		url : url,
		data : query,
		dataType : "json",
		success : function(data) {
			printGuest(data);
		},
		error : function(e) {
			console.log(e.responseText);
		}
	});
}

function printGuest(data) {
	var uid = "${sessionScope.member.userId}";
	
	var dataCount = data.dataCount;
	var total_page = data.total_page;
	var pageNo = data.pageNo;
	var paging = data.paging;
	
	var out="";
	var out="<table style='width: 100%; margin: 10px auto 0px; border-spacing: 0px; border-collapse: collapse;'>";
	out+="  <tr height='35'>";
	out+="     <td width='50%'>";
	out+="        <span style='color: #3EA9CD; font-weight: 700;'>방명록 "+dataCount+"개</span>";
	out+="        <span>[목록, "+pageNo+"/"+total_page+" 페이지]</span>";
	out+="      </td>";
	out+="      <td width='50%'>&nbsp;</td>";
	out+="   </tr>";
	
	if(dataCount!=0) {
		for(var idx=0; idx<data.list.length; idx++) {
			var num=data.list[idx].num;
			var userId=data.list[idx].userId;
			var userName=data.list[idx].userName;
			var content=data.list[idx].content;
			var created=data.list[idx].created;
			
			out+="    <tr height='35' bgcolor='#eeeeee'>";
			out+="      <td width='50%' style='padding-left: 5px; border:1px solid #cccccc; border-right:none;'>"+ userName+"</td>";
			out+="      <td width='50%' align='right' style='padding-right: 5px; border:1px solid #cccccc; border-left:none;'>" + created;
			if(uid==userId || uid=="admin") {
				out+=" | <a onclick='deleteGuest(\""+num+"\", \""+pageNo+"\");'>삭제</a></td>" ;
			} else {
				out+=" | <a href='#'>신고</a></td>" ;
			}
			out+="    </tr>";
			out+="    <tr style='height: 50px;'>";
			out+="      <td colspan='2' style='padding: 5px;' valign='top'>"+content+"</td>";
			out+="    </tr>";
		}
		out+="    <tr style='height: 35px;'>";
		out+="      <td colspan='2' style='text-align: center;'>";
		out+=paging;
		out+="      </td>";
		out+="    </tr>";
	}
	
	out+="</table>";
	
	$("#listGuest").html(out);
}

$(function(){
	$("#btnSend").click(function(){
		var uid = "${sessionScope.member.userId}";
		
		if(!uid) {
			location.href = "<%=cp%>/member/login.do";
			return;
		}
		
		if(!$("#content").val().trim()) {
			$("#content").focus();
			return;
		}
		
		//var query = "content=" + $("#content").val(); // ? 등 특수기호만 보내면 문제가 발생한다.
		var query = $("form[name=guestForm]").serialize(); // 직렬화를 한 경우 문제가 발생하지 않는다.
		var url = "<%=cp%>/guest/insert.do";
		
		$.ajax({
			type : "post",
			url : url,
			data : query,
			dataType : "json",
			success : function(data) {
				var state = data.state;
				
				if(state == "loginFail") {
					location.href = "<%=cp%>/member/login.do";
					return;
				}
				
				$("#content").val("");
				
				listPage(1);
			},
			error : function(e) {
				console.log(e.responseText);
			}
		});
	});
});

function deleteGuest(num, page) {
	if(confirm("게시물을 삭제 하시겠습니까?")) {
		var url = "<%=cp%>/guest/delete.do";
		
		$.post(url, {num : num}, function(data){
			var state = data.state;
			
			if(state == "loginFail") {
				location.href = "<%=cp%>/member/login.do";
				return;
			}
			
			listPage(page);
		}, "json"); // 서버측에 어떤 타입으로 받을것인지 지정
	}
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
                       <button type="button" id="btnSend" class="btn" style="padding:8px 25px;"> 등록하기 </button>
                  </div>           
            </div>
           </form>
         
           <div id="listGuest" style="width:100%; margin: 0px auto;"></div>
                    
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