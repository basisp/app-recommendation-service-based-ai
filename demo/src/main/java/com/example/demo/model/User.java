package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_USER")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NO")
    private Integer userNo;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "USER_PW")
    private String userPw;

    @Column(name = "USER_NAME", nullable = false)
    private String userName;

    @Column(name = "USER_AUTH", nullable = false)
    private String userAuth;

    @Column(name = "APPEND_DATE")
    private LocalDateTime appendDate;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    // Getters and setters

    public Integer getUserNo() {
        return userNo;
    }

    public void setUserNo(Integer userNo) {
        this.userNo = userNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPw() {
        return userPw;
    }

    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAuth() {
        return userAuth;
    }

    public void setUserAuth(String userAuth) {
        this.userAuth = userAuth;
    }

    public LocalDateTime getAppendDate() {
        return appendDate;
    }

    public void setAppendDate(LocalDateTime appendDate) {
        this.appendDate = appendDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }
}