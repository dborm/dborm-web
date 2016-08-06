CloudForm = function () {//构造函数

};


CloudForm.prototype = { //定义方法

    /**
     *
     * @param url  跳转URL(包含参数)
     */
    update: function (url) {
        window.location.href = url;
    },

    /**
     *
     * @param commitUrl     提交URL(包含参数)
     */
    deleted: function (commitUrl) {
        if (cloudFormCustom.deleteConfirm()) {
            $.ajax({
                type: "post",
                async: true,
                url: commitUrl,
                dataType: "json",
                success: function (data) {
                    cloudFormCustom.deleteSuccess(data, commitUrl);
                },
                error: function (data) {
                    cloudFormCustom.serverError(data);
                }
            });
        }
    },

    /**
     * 保存
     * @param formId        表单ID
     * @param redirectUrl   保存成功之后重定向的url
     */
    save: function (formId, redirectUrl) {
        var form = $("#" + formId);
        var action = form.attr("action");
        var datas = form.serialize();
        $.ajax({
            type: "post",
            async: true,
            url: action,
            dataType: "json",
            data: datas,
            success: function (data) {
                cloudFormCustom.saveSuccess(data, formId, redirectUrl);
            },
            error: function (data) {
                cloudFormCustom.serverError(data);
            }
        });
    }

};
cloudForm = new CloudForm();
