package org.dborm.web.demo;

import org.dborm.core.annotation.Column;
import org.dborm.core.annotation.Table;
import org.dborm.web.db.AppDomain;

/**
 * Created by shk
 */
@Table
public class UserInfo extends AppDomain {

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private Integer age;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
