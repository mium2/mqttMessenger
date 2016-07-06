<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

<form method="post" action="templategroupsendtest.ctl">
<table style="background:#808080;">
<tr>
	<td style="background:#c0c0ff;">
		<input type="submit" value="SEND" />
	</td>
	<td style="background:#c0c0ff;">
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		SENDERCODE
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="SENDERCODE" style="width:200px;" value="sender" />
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		GROUPSEQ
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="GROUPSEQ" style="width:200px;" value="251" />
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		APP_ID
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="APP_ID" style="width:200px;" value="com.test.ClientTest" />
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		TEMPLATETYPE
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="TEMPLATETYPE" style="width:200px;" value="C" />
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		TEMPLATEDATA
	</td>
	<td style="background:#ffffff;">
		<textarea name="TEMPLATEDATA" style="width:400px; height:200px;">T_0001</textarea>
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		SOUNDFILE
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="SOUNDFILE" style="width:200px;" value="alert.aif" />
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		BADGENO
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="BADGENO" style="width:200px;" value="1" />
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		PRIORITY
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="PRIORITY" style="width:200px;" value="3" />
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		RESERVEDATE
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="RESERVEDATE" style="width:200px;" value="2013/05/15 13:00:00" />
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		TYPE
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="TYPE" style="width:200px;" value="G" />
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		EXT
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="EXT" style="width:200px;" value="PAGENO=1" />
	</td>
</tr>
<tr>
	<td style="background:#c0c0ff;">
		SERVICECODE
	</td>
	<td style="background:#ffffff;">
		<input type="text" name="SERVICECODE" style="width:200px;" value="0002" />
	</td>
</tr>

</table>
</form>
</body>
</html>