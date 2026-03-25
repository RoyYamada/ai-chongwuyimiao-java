package com.example.demo.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class WxSessionRepository {
    private final JdbcTemplate jdbcTemplate;

    public WxSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsert(String openid, String sessionKey, String unionid, Instant expireAt) {
        jdbcTemplate.update(
                "insert into wx_session(openid, session_key, unionid, expire_at, updated_at) values(?,?,?,?,now()) " +
                        "on conflict(openid) do update set session_key=excluded.session_key, unionid=excluded.unionid, expire_at=excluded.expire_at, updated_at=now()",
                openid, sessionKey, unionid, Timestamp.from(expireAt)
        );
    }

    public String getSessionKey(String openid) {
        List<String> list = jdbcTemplate.query("select session_key from wx_session where openid=?", (rs, i) -> rs.getString(1), openid);
        return list.isEmpty() ? null : list.get(0);
    }
}
