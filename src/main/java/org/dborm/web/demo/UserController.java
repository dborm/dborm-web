package org.dborm.web.demo;

import org.dborm.core.framework.Dborm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by sky
 */
@Controller()
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    Dborm dborm;

    @Autowired
    UserService userService;

    @ResponseBody
    @RequestMapping(value = "/getList")
    public List<UserInfo> getList(UserInfo userInfo) {
        return userService.getList(userInfo);
    }

    @RequestMapping(value = "/toEdit")
    public String toEdit(UserInfo userInfo, ModelMap model) {
        userInfo = dborm.getEntityByExample(userInfo);
        model.put("userInfo", userInfo);
        return "/view/user_edit.jsp";
    }

    @ResponseBody
    @RequestMapping(value = "/save")
    public boolean save(UserInfo userInfo) {
        return dborm.saveOrUpdate(userInfo);
    }

    @ResponseBody
    @RequestMapping(value = "/delete")
    public boolean delete(UserInfo userInfo) {
        return dborm.delete(userInfo);
    }


}
