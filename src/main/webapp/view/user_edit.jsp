<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String path = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <script src="<%=path%>/res/jquery-2.2.2.min.js" type="text/javascript"></script>
    <script src="<%=path%>/res/cloud-form.js" type="text/javascript"></script>
    <script src="<%=path%>/res/cloud-form-customer.js" type="text/javascript"></script>
    <script src="<%=path%>/view/user_edit.js" type="text/javascript"></script>


</head>


<body>
<div style="text-align: center;">
    <h2>编辑用户信息</h2>
    <form id="edit_form" action="<%=path%>/control//user/save.do">
        <input type="hidden" id="id" name="id" value="${userInfo.id}">
        用户名:<input id="username" name="username" value="${userInfo.username}"><br><br>
        密  码:<input id="password" name="password" value="${userInfo.password}"><br><br>
        年  龄:<input id="age" name="age" value="${userInfo.age}"><br><br>
        <input type="reset" value="重置"/>
        <input type="button" onclick="save()" value="提交"/>
    </form>


</div>
</body>
</html>
