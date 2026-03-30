package com.example.demo.pet;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

@Repository
public class ReminderRepository {
    private final JdbcTemplate jdbcTemplate;

    public ReminderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(Reminder r) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into reminder(template_id,vaccination_id,reminder_date,reminder_thing,location,target_name,notes,created_at,updated_at) values(?,?,?,?,?,?,?,now(),now())", new String[]{"id"});
            ps.setString(1, r.getTemplateId());
            ps.setLong(2, r.getVaccinationId());
            ps.setDate(3, r.getReminderDate() == null ? null : java.sql.Date.valueOf(r.getReminderDate()));
            ps.setString(4, r.getReminderThing());
            ps.setString(5, r.getLocation());
            ps.setString(6, r.getTargetName());
            ps.setString(7, r.getNotes());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public List<Reminder> listByVaccination(Long vaccinationId) {
        return jdbcTemplate.query("select id,template_id,vaccination_id,reminder_date,reminder_thing,location,target_name,notes,created_at,updated_at from reminder where vaccination_id=? order by reminder_date asc",
                (rs, i) -> {
                    Reminder r = new Reminder();
                    r.setId(rs.getLong("id"));
                    r.setTemplateId(rs.getString("template_id"));
                    r.setVaccinationId(rs.getLong("vaccination_id"));
                    r.setReminderDate(rs.getDate("reminder_date") == null ? null : rs.getDate("reminder_date").toLocalDate());
                    r.setReminderThing(rs.getString("reminder_thing"));
                    r.setLocation(rs.getString("location"));
                    r.setTargetName(rs.getString("target_name"));
                    r.setNotes(rs.getString("notes"));
                    r.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    r.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                    return r;
                }, vaccinationId);
    }

    public List<Reminder> listByDate(LocalDate date) {
        return jdbcTemplate.query("select id,template_id,vaccination_id,reminder_date,reminder_thing,location,target_name,notes,created_at,updated_at from reminder where reminder_date=? order by id asc",
                (rs, i) -> {
                    Reminder r = new Reminder();
                    r.setId(rs.getLong("id"));
                    r.setTemplateId(rs.getString("template_id"));
                    r.setVaccinationId(rs.getLong("vaccination_id"));
                    r.setReminderDate(rs.getDate("reminder_date") == null ? null : rs.getDate("reminder_date").toLocalDate());
                    r.setReminderThing(rs.getString("reminder_thing"));
                    r.setLocation(rs.getString("location"));
                    r.setTargetName(rs.getString("target_name"));
                    r.setNotes(rs.getString("notes"));
                    r.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    r.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                    return r;
                }, java.sql.Date.valueOf(date));
    }

    public Reminder findById(Long id) {
        return jdbcTemplate.queryForObject("select id,template_id,vaccination_id,reminder_date,reminder_thing,location,target_name,notes,created_at,updated_at from reminder where id=?",
                (rs, i) -> {
                    Reminder r = new Reminder();
                    r.setId(rs.getLong("id"));
                    r.setTemplateId(rs.getString("template_id"));
                    r.setVaccinationId(rs.getLong("vaccination_id"));
                    r.setReminderDate(rs.getDate("reminder_date") == null ? null : rs.getDate("reminder_date").toLocalDate());
                    r.setReminderThing(rs.getString("reminder_thing"));
                    r.setLocation(rs.getString("location"));
                    r.setTargetName(rs.getString("target_name"));
                    r.setNotes(rs.getString("notes"));
                    r.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    r.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                    return r;
                }, id);
    }

    public void update(Reminder r) {
        jdbcTemplate.update(
                "update reminder set template_id=?, vaccination_id=?, reminder_date=?, reminder_thing=?, location=?, target_name=?, notes=?, updated_at=now() where id=?",
                r.getTemplateId(),
                r.getVaccinationId(),
                r.getReminderDate() == null ? null : java.sql.Date.valueOf(r.getReminderDate()),
                r.getReminderThing(),
                r.getLocation(),
                r.getTargetName(),
                r.getNotes(),
                r.getId()
        );
    }

    public void delete(Long id) {
        jdbcTemplate.update("delete from reminder where id=?", id);
    }
}