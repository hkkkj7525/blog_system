package com.example.blog_system.dao;

public class UserStatistics {
    private Integer totalUsers;
    private Integer activeUsers;
    private Integer disabledUsers;

    // getter和setter方法
    public Integer getTotalUsers() {
        return totalUsers;
    }
    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Integer getActiveUsers() {
        return activeUsers;
    }
    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Integer getDisabledUsers() {
        return disabledUsers;
    }
    public void setDisabledUsers(Integer disabledUsers) {
        this.disabledUsers = disabledUsers;
    }
}