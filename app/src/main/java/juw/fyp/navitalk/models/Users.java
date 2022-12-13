package juw.fyp.navitalk.models;

import java.util.List;

public class Users {
    private String UserId;
    private String UserName;
    private String mail;
    private String Role;
    private List<String> code;
 //   private Long code;

    public Users(){}

//    public Long getCode() {
//        return code;
//    }
//
//    public void setCode(Long code) {
//        this.code = code;
//    }


    public List<String> getCode() {
        return code;
    }

    public void setCode(List<String> code) {
        this.code = code;
    }

    public String getUserId() {
        return UserId;
    }

    public String getUserName() {
        return UserName;
    }

    public String getMail() {
        return mail;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

}
