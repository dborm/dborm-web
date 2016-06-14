CloudFormCustom = function () {//构造函数

};

CloudFormCustom.prototype = { //定义方法

    deleteConfirm: function () {
        return confirm("您确定要删除该条记录么?");
    },

    deleteSuccess: function (data, commitUrl) {
        if (data) {
            alert("删除成功!");
            query();
        } else {
            alert("删除失败!");
        }
    },

    saveSuccess: function (data, formId, redirectUrl) {
        if (data) {
            alert("保存成功");
            window.location.href = redirectUrl;
        } else {
            alert("保存失败");
        }
    },

    serverError: function (data) {
        alert("请求服务器出错!" + data);
    }

};
cloudFormCustom = new CloudFormCustom();
