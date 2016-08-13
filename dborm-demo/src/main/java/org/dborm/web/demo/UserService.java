package org.dborm.web.demo;

import org.dborm.core.api.Dborm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shk
 */
@Service
public class UserService {

    @Autowired
    Dborm dborm;

    public List<UserInfo> getList(UserInfo userInfo) {
        StringBuffer sql = new StringBuffer("SELECT * FROM user_info WHERE deleted = 0");
        List args = new ArrayList();
        if (userInfo.getUsername() != null && userInfo.getUsername().length() > 0) {
            sql.append(" AND username LIKE ?");
            args.add("%" + userInfo.getUsername() + "%");
        }
        sql.append(" ORDER BY update_time DESC");
        return dborm.getEntities(UserInfo.class, sql.toString(), args);
    }

}
