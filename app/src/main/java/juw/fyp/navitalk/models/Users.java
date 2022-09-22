package juw.fyp.navitalk.models;

public class Users {
    private String UserId;
    private String UserName;
    private String mail;
    private String Role;
    private String code;

    public Users(){}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
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
