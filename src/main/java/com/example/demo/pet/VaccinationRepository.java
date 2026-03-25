package com.example.demo.pet;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class VaccinationRepository {
    private final JdbcTemplate jdbcTemplate;

    public VaccinationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(Vaccination r) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into vaccination(pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status) values(?,?,?,?,?,?,?,?,?)", new String[]{"id"});
            ps.setLong(1, r.getPetId());
            ps.setLong(2, r.getVaccineId());
            ps.setInt(3, r.getDoseNumber());
            ps.setTimestamp(4, r.getAdministeredAt() == null ? Timestamp.from(Instant.now()) : Timestamp.from(r.getAdministeredAt()));
            ps.setString(5, r.getLotNumber());
            ps.setString(6, r.getClinic());
            ps.setString(7, r.getVetName());
            ps.setTimestamp(8, r.getNextDueAt() == null ? null : Timestamp.from(r.getNextDueAt()));
            ps.setString(9, r.getStatus());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public List<Vaccination> listByPet(Long petId) {
        return jdbcTemplate.query("select id,pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status from vaccination where pet_id=? order by administered_at desc",
                (rs, i) -> {
                    Vaccination r = new Vaccination();
                    r.setId(rs.getLong("id"));
                    r.setPetId(rs.getLong("pet_id"));
                    r.setVaccineId(rs.getLong("vaccine_id"));
                    r.setDoseNumber(rs.getInt("dose_number"));
                    r.setAdministeredAt(rs.getTimestamp("administered_at").toInstant());
                    r.setLotNumber(rs.getString("lot_number"));
                    r.setClinic(rs.getString("clinic"));
                    r.setVetName(rs.getString("vet_name"));
                    r.setNextDueAt(rs.getTimestamp("next_due_at") == null ? null : rs.getTimestamp("next_due_at").toInstant());
                    r.setStatus(rs.getString("status"));
                    return r;
                }, petId);
    }

    public List<Vaccination> listDueUntil(Instant to) {
        return jdbcTemplate.query("select id,pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status from vaccination where next_due_at is not null and next_due_at<=? order by next_due_at asc",
                (rs, i) -> {
                    Vaccination r = new Vaccination();
                    r.setId(rs.getLong("id"));
                    r.setPetId(rs.getLong("pet_id"));
                    r.setVaccineId(rs.getLong("vaccine_id"));
                    r.setDoseNumber(rs.getInt("dose_number"));
                    r.setAdministeredAt(rs.getTimestamp("administered_at").toInstant());
                    r.setLotNumber(rs.getString("lot_number"));
                    r.setClinic(rs.getString("clinic"));
                    r.setVetName(rs.getString("vet_name"));
                    r.setNextDueAt(rs.getTimestamp("next_due_at") == null ? null : rs.getTimestamp("next_due_at").toInstant());
                    r.setStatus(rs.getString("status"));
                    return r;
                }, Timestamp.from(to));
    }
}
