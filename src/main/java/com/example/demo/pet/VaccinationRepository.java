package com.example.demo.pet;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class VaccinationRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public VaccinationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String toDateTimeString(LocalDateTime ldt) {
        return ldt != null ? ldt.format(FORMATTER) : null;
    }

    private LocalDateTime toLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    public Long create(Vaccination r) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into vaccination(pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status,notes,is_unvaccinated) values(?,?,?,?,?,?,?,?,?,?,?)", new String[]{"id"});
            ps.setLong(1, r.getPetId());
            ps.setLong(2, r.getVaccineId());
            ps.setInt(3, r.getDoseNumber());
            ps.setString(4, r.getAdministeredAt());
            ps.setString(5, r.getLotNumber());
            ps.setString(6, r.getClinic());
            ps.setString(7, r.getVetName());
            ps.setString(8, r.getNextDueAt());
            ps.setString(9, r.getStatus());
            ps.setString(10, r.getNotes());
            ps.setBoolean(11, r.getIsUnvaccinated() == null ? false : r.getIsUnvaccinated());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public List<Vaccination> listByPet(Long petId) {
        return jdbcTemplate.query("select id,pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status,notes,is_unvaccinated from vaccination where pet_id=? order by administered_at desc",
                this::mapRow, petId);
    }

    public List<Vaccination> listDueUntil(String to, int page, int size) {
        int offset = (page - 1) * size;
        return jdbcTemplate.query("select id,pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status,notes,is_unvaccinated from vaccination where status='COMPLETED' order by administered_at desc limit ? offset ?",
                this::mapRow, size, offset);
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
                this::mapRow, ownerId, size, offset);
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
                this::mapRow);
    }

    public List<Vaccination> listFutureDueByOwner(Long ownerId) {
        return jdbcTemplate.query(
                "select v.id,v.pet_id,v.vaccine_id,v.dose_number,v.administered_at,v.lot_number,v.clinic,v.vet_name,v.next_due_at,v.status,v.notes,v.is_unvaccinated " +
                        "from vaccination v join pet p on v.pet_id=p.id " +
                        "where v.next_due_at is not null and v.status='PENDING' and p.owner_id=? " +
                        "order by v.next_due_at asc",
                this::mapRow, ownerId);
    }

    public Vaccination findById(Long id) {
        return jdbcTemplate.queryForObject("select id,pet_id,vaccine_id,dose_number,administered_at,lot_number,clinic,vet_name,next_due_at,status,notes,is_unvaccinated from vaccination where id=?",
                this::mapRow, id);
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
                v.getAdministeredAt(),
                v.getLotNumber(),
                v.getClinic(),
                v.getVetName(),
                v.getNextDueAt(),
                v.getStatus(),
                v.getNotes(),
                v.getIsUnvaccinated() == null ? false : v.getIsUnvaccinated(),
                v.getId()
        );
    }

    private Vaccination mapRow(ResultSet rs, int rowNum) throws SQLException {
        Vaccination r = new Vaccination();
        r.setId(rs.getLong("id"));
        r.setPetId(rs.getLong("pet_id"));
        r.setVaccineId(rs.getLong("vaccine_id"));
        r.setDoseNumber(rs.getInt("dose_number"));
        r.setAdministeredAt(rs.getString("administered_at"));
        r.setLotNumber(rs.getString("lot_number"));
        r.setClinic(rs.getString("clinic"));
        r.setVetName(rs.getString("vet_name"));
        r.setNextDueAt(rs.getString("next_due_at"));
        r.setStatus(rs.getString("status"));
        r.setNotes(rs.getString("notes"));
        r.setIsUnvaccinated(rs.getBoolean("is_unvaccinated"));
        return r;
    }
}