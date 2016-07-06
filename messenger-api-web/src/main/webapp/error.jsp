<!-- 
//Common Error Page by UncleJoe 
 -->
<%@ page contentType="text/html; charset=utf-8" isErrorPage="true"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%
	String rootPath = request.getContextPath();
	String errorCode = "500"; 
	if(request.getParameter("ERROR") != null){
    	errorCode = request.getParameter("ERROR");
	}
%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta http-equiv="content-type" content="applicaton/xhtml+xml;charset=utf-8" />
<title>MSP Receiver</title>
<script type="text/javascript">
</script>
</head>

<body style="background-image:none;">

<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td height="100"></td>
	</tr>
  <tr>
    <td align="center">
    	<table width="500" border="0" cellspacing="0" cellpadding="0">
      		<tr>
        		<td height="200" align="center" valign="top">
		        	<table width="400" border="0" cellspacing="0" cellpadding="0">
		          		<tr>
		            		<td height="160">&nbsp;</td>
		          		</tr>
		          		<tr>
		            		<td align="center">"처리중 오류가 발생했습니다. 관리자에게 문의하세요!"</td>
		          		</tr>
		          		<tr>
		            		<td align="center" height="14"></td>
		          		</tr>
		          		
		        	</table>
        		</td>
      		</tr>
    	</table>
    </td> 
  </tr>
</table>
</body>
</html>
