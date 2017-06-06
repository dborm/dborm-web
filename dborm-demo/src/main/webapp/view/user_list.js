$(function () {
    query();//页面加载完毕之后执行查询,初始化页面数据
});
function query() {
    $.ajax({
        type: 'post',
        url: "/dborm-web/control/user/getList.do",
        data: $("#query_form").serialize(),
        async: true,
        success: function (data) {
            initData(data);
        },
        error: function () {
            alert('请求服务器出错！');
        }
    });
}

function initData(data) {
    $("#dataContent").html("");//清除之前的内容
    //将获取到的数据动态的加载到table中
    for (var i = 0; i < data.length; i++) {
        var obj = data[i];
        var row = $("#dataModel").clone();//复制一行
        row.show();
        row.find("#name").html(obj.username);
        row.find("#age").html(obj.age);
        var time = new Date(obj.modifyTime);
        row.find("#createTime").html(time.format('yyyy-MM-dd hh:mm:ss'));
        row.find("#update").attr("href", "/dborm-web/control/user/toEdit?id=" + obj.id);
        row.find("#delete").attr("onclick", "deleteUser('" + obj.id + "')");
        //将新行添加到表格中
        row.appendTo("#dataContent");
    }
}

function deleteUser(id) {
    $.ajax({
        url: "/dborm-web/control/user/delete.do",
        type: 'post',
        data: {"id": id},
        async: true,
        success: function (data) {
            if (data == true) {
                query();
            } else {
                alert("删除失败!");
            }
        },
        error: function () {
            alert('请求服务器出错！');
        }
    });
}


Date.prototype.format = function(format) {
    var date = {
        "M+": this.getMonth() + 1,
        "d+": this.getDate(),
        "h+": this.getHours(),
        "m+": this.getMinutes(),
        "s+": this.getSeconds(),
        "q+": Math.floor((this.getMonth() + 3) / 3),
        "S+": this.getMilliseconds()
    };
    if (/(y+)/i.test(format)) {
        format = format.replace(RegExp.$1, (this.getFullYear() + '').substr(4 - RegExp.$1.length));
    }
    for (var k in date) {
        if (new RegExp("(" + k + ")").test(format)) {
            format = format.replace(RegExp.$1, RegExp.$1.length == 1
                ? date[k] : ("00" + date[k]).substr(("" + date[k]).length));
        }
    }
    return format;
}