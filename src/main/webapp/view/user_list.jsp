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
    <script src="<%=path%>/view/user_list.js" type="text/javascript"></script>


</head>


<body>
<div style="text-align: center;">
    <h2>查询案例</h2>
    <br>
    <br>
    <a href="<%=path%>/view/user_edit.jsp">新增数据</a>
    <br>
    <br>
    <form id="query_form">
        <input type="text" name="username">
        <input type="button" onclick="query()" value="查询">
    </form>
    <br>
    <br>

    <table border="1" cellpadding="1" cellspacing="0" style="width: 60%;margin:auto">
        <thead>
        <td width="100px">姓名</td>
        <td width="50px">年龄</td>
        <td width="150px">信息修改时间</td>
        <td width="80px">操作</td>
        </thead>
        <tbody id="dataContent">
        </tbody>
        <tr id="dataModel" style="display: none">
            <td id="name">sky</td>
            <td id="age">28</td>
            <td id="createTime">2016-10-10 11:11:11</td>
            <td>
                <a id="update" href="">修改</a>
                <a id="delete" href="javascript:void(0);" onclick="">删除</a>
            </td>
        </tr>
    </table>

</div>
</body>
</html>
