package com.example.demo.auth;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "wx_session")
public class WxSession {
    @Id
    @Column(name = "openid", length = 64)
    private String openid;

    @Column(name = "session_key", length = 128, nullable = false)
    private String sessionKey;

    @Column(name = "unionid", length = 64)
    private String unionid;

    @Column(name = "expire_at")
    private Instant expireAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Getters and setters
    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public Instant getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Instant expireAt) {
        this.expireAt = expireAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}