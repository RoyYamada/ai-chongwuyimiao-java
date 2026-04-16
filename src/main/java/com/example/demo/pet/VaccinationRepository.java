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
            PreparedStatement ps = con.prepareStatement("insert into vaccination(pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status,notes,is_unvaccinated) values(?,?,?,?,?,?,?,?,?,?,?)", new String[]{"id"});
            ps.setLong(1, r.getPetId());
            ps.setLong(2, r.getVaccineId());
            ps.setInt(3, r.getDoseNumber());
            boolean isUnvaccinated = r.getIsUnvaccinated() == null ? false : r.getIsUnvaccinated();
            ps.setTimestamp(4, r.getAdministeredAt() == null ? Timestamp.from(Instant.now()) : Timestamp.from(r.getAdministeredAt()));
            ps.setString(5, r.getLotNumber());
            ps.setString(6, r.getClinic());
            ps.setString(7, r.getVetName());
            ps.setTimestamp(8, r.getNextDueAt() == null ? null : Timestamp.from(r.getNextDueAt()));
            ps.setString(9, r.getStatus());
            ps.setString(10, r.getNotes());
            ps.setBoolean(11, r.getIsUnvaccinated() == null ? false : r.getIsUnvaccinated());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public List<Vaccination> listByPet(Long petId) {
        return jdbcTemplate.query("select id,pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status,notes,is_unvaccinated from vaccination where pet_id=? order by administered_at desc",
                (rs, i) -> {
                    Vaccination r = new Vaccination();
                    r.setId(rs.getLong("id"));
                    r.setPetId(rs.getLong("pet_id"));
                    r.setVaccineId(rs.getLong("vaccine_id"));
                    r.setDoseNumber(rs.getInt("dose_number"));
                    r.setAdministeredAt(rs.getTimestamp("administered_at") == null ? null : rs.getTimestamp("administered_at").toInstant());
                    r.setLotNumber(rs.getString("lot_number"));
                    r.setClinic(rs.getString("clinic"));
                    r.setVetName(rs.getString("vet_name"));
                    r.setNextDueAt(rs.getTimestamp("next_due_at") == null ? null : rs.getTimestamp("next_due_at").toInstant());
                    r.setStatus(rs.getString("status"));
                    r.setNotes(rs.getString("notes"));
                    r.setIsUnvaccinated(rs.getBoolean("is_unvaccinated"));
                    return r;
                }, petId);
    }

    public List<Vaccination> listDueUntil(Instant to, int page, int size) {
        int offset = (page - 1) * size;
        return jdbcTemplate.query("select id,pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status,notes,is_unvaccinated from vaccination where status='COMPLETED' order by administered_at desc limit ? offset ?",
                (rs, i) -> {
                    Vaccination r = new Vaccination();
                    r.setId(rs.getLong("id"));
                    r.setPetId(rs.getLong("pet_id"));
                    r.setVaccineId(rs.getLong("vaccine_id"));
                    r.setDoseNumber(rs.getInt("dose_number"));
                    r.setAdministeredAt(rs.getTimestamp("administered_at") == null ? null : rs.getTimestamp("administered_at").toInstant());
                    r.setLotNumber(rs.getString("lot_number"));
                    r.setClinic(rs.getString("clinic"));
                    r.setVetName(rs.getString("vet_name"));
                    r.setNextDueAt(rs.getTimestamp("next_due_at") == null ? null : rs.getTimestamp("next_due_at").toInstant());
                    r.setStatus(rs.getString("status"));
                    r.setNotes(rs.getString("notes"));
                    r.setIsUnvaccinated(rs.getBoolean("is_unvaccinated"));
                    return r;
                }, size, offset);
    }

    public int countCompletedVaccinations() {
        return jdbcTemplate.queryForObject("select count(*) from vaccination where status='COMPLETED'", Integer.class);
    }

    public List<Vaccination> listDueCompletedByOwner(Long ownerId, int page, int size) {
        int offset = (page - 1) * size;
        return jdbcTemplate.query(
                "select v.id,v.pet_id,v.vaccine_id,v.dose_number,v.administered_at,v.lot_number,v.clinic,v.vet_name,v.next_due_at,v.status,v.notes,v.is_unvaccinated " +
                        "from vaccination v join pet p on v.pet_id=p.id " +
                        "where v.status='COMPLETED' and p.owner_id=? " +
                        "order by v.administered_at desc limit ? offset ?",
                (rs, i) -> {
                    Vaccination r = new Vaccination();
                    r.setId(rs.getLong("id"));
                    r.setPetId(rs.getLong("pet_id"));
                    r.setVaccineId(rs.getLong("vaccine_id"));
                    r.setDoseNumber(rs.getInt("dose_number"));
                    r.setAdministeredAt(rs.getTimestamp("administered_at") == null ? null : rs.getTimestamp("administered_at").toInstant());
                    r.setLotNumber(rs.getString("lot_number"));
                    r.setClinic(rs.getString("clinic"));
                    r.setVetName(rs.getString("vet_name"));
                    r.setNextDueAt(rs.getTimestamp("next_due_at") == null ? null : rs.getTimestamp("next_due_at").toInstant());
                    r.setStatus(rs.getString("status"));
                    r.setNotes(rs.getString("notes"));
                    r.setIsUnvaccinated(rs.getBoolean("is_unvaccinated"));
                    return r;
                }, ownerId, size, offset);
    }

    public int countCompletedVaccinationsByOwner(Long ownerId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from vaccination v join pet p on v.pet_id=p.id where v.status='COMPLETED' and p.owner_id=?",
                Integer.class,
                ownerId
        );
    }

    public List<Vaccination> listFutureDue() {
        return jdbcTemplate.query("select id,pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status,notes,is_unvaccinated from vaccination where next_due_at is not null order by next_due_at asc",
                (rs, i) -> {
                    Vaccination r = new Vaccination();
                    r.setId(rs.getLong("id"));
                    r.setPetId(rs.getLong("pet_id"));
                    r.setVaccineId(rs.getLong("vaccine_id"));
                    r.setDoseNumber(rs.getInt("dose_number"));
                    r.setAdministeredAt(rs.getTimestamp("administered_at") == null ? null : rs.getTimestamp("administered_at").toInstant());
                    r.setLotNumber(rs.getString("lot_number"));
                    r.setClinic(rs.getString("clinic"));
                    r.setVetName(rs.getString("vet_name"));
                    r.setNextDueAt(rs.getTimestamp("next_due_at") == null ? null : rs.getTimestamp("next_due_at").toInstant());
                    r.setStatus(rs.getString("status"));
                    r.setNotes(rs.getString("notes"));
                    r.setIsUnvaccinated(rs.getBoolean("is_unvaccinated"));
                    return r;
                });
    }

    public List<Vaccination> listFutureDueByOwner(Long ownerId) {
        return jdbcTemplate.query(
                "select v.id,v.pet_id,v.vaccine_id,v.dose_number,v.administered_at,v.lot_number,v.clinic,v.vet_name,v.next_due_at,v.status,v.notes,v.is_unvaccinated " +
                        "from vaccination v join pet p on v.pet_id=p.id " +
                        "where v.next_due_at is not null and v.status='PENDING' and p.owner_id=? " +
                        "order by v.next_due_at asc",
                (rs, i) -> {
                    Vaccination r = new Vaccination();
                    r.setId(rs.getLong("id"));
                    r.setPetId(rs.getLong("pet_id"));
                    r.setVaccineId(rs.getLong("vaccine_id"));
                    r.setDoseNumber(rs.getInt("dose_number"));
                    r.setAdministeredAt(rs.getTimestamp("administered_at") == null ? null : rs.getTimestamp("administered_at").toInstant());
                    r.setLotNumber(rs.getString("lot_number"));
                    r.setClinic(rs.getString("clinic"));
                    r.setVetName(rs.getString("vet_name"));
                    r.setNextDueAt(rs.getTimestamp("next_due_at") == null ? null : rs.getTimestamp("next_due_at").toInstant());
                    r.setStatus(rs.getString("status"));
                    r.setNotes(rs.getString("notes"));
                    r.setIsUnvaccinated(rs.getBoolean("is_unvaccinated"));
                    return r;
                }, ownerId);
    }

    public Vaccination findById(Long id) {
        return jdbcTemplate.queryForObject("select id,pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status,notes,is_unvaccinated from vaccination where id=?",
                (rs, i) -> {
                    Vaccination r = new Vaccination();
                    r.setId(rs.getLong("id"));
                    r.setPetId(rs.getLong("pet_id"));
                    r.setVaccineId(rs.getLong("vaccine_id"));
                    r.setDoseNumber(rs.getInt("dose_number"));
                    r.setAdministeredAt(rs.getTimestamp("administered_at") == null ? null : rs.getTimestamp("administered_at").toInstant());
                    r.setLotNumber(rs.getString("lot_number"));
                    r.setClinic(rs.getString("clinic"));
                    r.setVetName(rs.getString("vet_name"));
                    r.setNextDueAt(rs.getTimestamp("next_due_at") == null ? null : rs.getTimestamp("next_due_at").toInstant());
                    r.setStatus(rs.getString("status"));
                    r.setNotes(rs.getString("notes"));
                    r.setIsUnvaccinated(rs.getBoolean("is_unvaccinated"));
                    return r;
                }, id);
    }

    public void updateStatus(Long id, String status) {
        jdbcTemplate.update("update vaccination set status=? where id=?", status, id);
    }

    public void update(Vaccination v) {
        jdbcTemplate.update(
                "update vaccination set pet_id=?, vaccine_id=?, dose_number=?, administered_at=?, lot_number=?, clinic=?, vet_name=?, next_due_at=?, status=?, notes=?, is_unvaccinated=? where id=?",
                v.getPetId(),
                v.getVaccineId(),
                v.getDoseNumber(),
                v.getAdministeredAt() == null ? null : java.sql.Timestamp.from(v.getAdministeredAt()),
                v.getLotNumber(),
                v.getClinic(),
                v.getVetName(),
                v.getNextDueAt() == null ? null : java.sql.Timestamp.from(v.getNextDueAt()),
                v.getStatus(),
                v.getNotes(),
                v.getIsUnvaccinated() == null ? false : v.getIsUnvaccinated(),
                v.getId()
        );
    }
}
