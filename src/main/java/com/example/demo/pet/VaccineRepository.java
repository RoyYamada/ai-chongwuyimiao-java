package com.example.demo.pet;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class VaccineRepository {
    private final JdbcTemplate jdbcTemplate;

    public VaccineRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(Vaccine v) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into vaccine(name,species,doses_required,interval_days,valid_months) values(?,?,?,?,?)", new String[]{"id"});
            ps.setString(1, v.getName());
            ps.setString(2, v.getSpecies());
            ps.setObject(3, v.getDosesRequired());
            ps.setObject(4, v.getIntervalDays());
            ps.setObject(5, v.getValidMonths());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public List<Vaccine> list(String species) {
        if (species == null || species.isEmpty()) {
            return jdbcTemplate.query("select id,name,species,doses_required,interval_days,valid_months from vaccine order by id desc",
                    (rs, i) -> {
                        Vaccine v = new Vaccine();
                        v.setId(rs.getLong("id"));
                        v.setName(rs.getString("name"));
                        v.setSpecies(rs.getString("species"));
                        v.setDosesRequired((Integer) rs.getObject("doses_required"));
                        v.setIntervalDays((Integer) rs.getObject("interval_days"));
                        v.setValidMonths((Integer) rs.getObject("valid_months"));
                        return v;
                    });
        } else {
            return jdbcTemplate.query("select id,name,species,doses_required,interval_days,valid_months from vaccine where species=? order by id desc",
                    (rs, i) -> {
                        Vaccine v = new Vaccine();
                        v.setId(rs.getLong("id"));
                        v.setName(rs.getString("name"));
                        v.setSpecies(rs.getString("species"));
                        v.setDosesRequired((Integer) rs.getObject("doses_required"));
                        v.setIntervalDays((Integer) rs.getObject("interval_days"));
                        v.setValidMonths((Integer) rs.getObject("valid_months"));
                        return v;
                    }, species);
        }
    }
}
