package com.example.demo.pet;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class OwnerRepository {
    private final JdbcTemplate jdbcTemplate;

    public OwnerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(Owner o) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into owner(name, phone, email) values(?,?,?)", new String[]{"id"});
            ps.setString(1, o.getName());
            ps.setString(2, o.getPhone());
            ps.setString(3, o.getEmail());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public Optional<Owner> findById(Long id) {
        List<Owner> list = jdbcTemplate.query("select id,name,phone,email from owner where id=?",
                (rs, i) -> {
                    Owner o = new Owner();
                    o.setId(rs.getLong("id"));
                    o.setName(rs.getString("name"));
                    o.setPhone(rs.getString("phone"));
                    o.setEmail(rs.getString("email"));
                    return o;
                }, id);
        return list.stream().findFirst();
    }
}
