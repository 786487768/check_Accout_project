<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>测试sprig mvc方式上传付款信息</title>
</head>
<body>
	<form action="/check_Accout/ConnectP_Pay_Controller/upload_pay_2" enctype="multipart/form-data" method="post">
		付款人：<input type="text" name="payer" value="邓航"><br>
		金额：<input type="text" name="pay_money" value="1000"><br>
		付款方式<select name="pay_way">
			 	<option value="网银转账">网银转账</option>
			  	<option value="柜台电汇">柜台电汇</option>
			  	<option value="现金">现金</option>
			  	<option value="银行承兑汇票">银行承兑汇票</option>
			 </select><br>
		账号<input type="text" name="pay_account" value="443439008"><br>
		接受人：<input type="text" name="receiver" value="代理A"><br>
		<input type="file" name="file" id="fileid"><br>
		<input type="submit" name="提交">
	</form>
</body>
</html>