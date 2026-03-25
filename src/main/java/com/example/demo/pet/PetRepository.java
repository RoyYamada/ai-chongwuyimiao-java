package com.example.demo.pet;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class PetRepository {
    private final JdbcTemplate jdbcTemplate;

    public PetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(Pet p) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into pet(owner_id,name,species,breed,gender,birth_date,weight_kg,microchip) values(?,?,?,?,?,?,?,?)", new String[]{"id"});
            ps.setLong(1, p.getOwnerId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getSpecies());
            ps.setString(4, p.getBreed());
            ps.setString(5, p.getGender());
            ps.setObject(6, p.getBirthDate() == null ? null : Date.valueOf(p.getBirthDate()));
            if (p.getWeightKg() == null) ps.setObject(7, null); else ps.setDouble(7, p.getWeightKg());
            ps.setString(8, p.getMicrochip());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public Optional<Pet> findById(Long id) {
        List<Pet> list = jdbcTemplate.query("select id,owner_id,name,species,breed,gender,birth_date,weight_kg,microchip from pet where id=?",
                (rs, i) -> {
                    Pet p = new Pet();
                    p.setId(rs.getLong("id"));
                    p.setOwnerId(rs.getLong("owner_id"));
                    p.setName(rs.getString("name"));
                    p.setSpecies(rs.getString("species"));
                    p.setBreed(rs.getString("breed"));
                    p.setGender(rs.getString("gender"));
                    p.setBirthDate(rs.getDate("birth_date") == null ? null : rs.getDate("birth_date").toLocalDate());
                    p.setWeightKg(rs.getObject("weight_kg") == null ? null : rs.getDouble("weight_kg"));
                    p.setMicrochip(rs.getString("microchip"));
                    return p;
                }, id);
        return list.stream().findFirst();
    }

    public List<Pet> listByOwner(Long ownerId) {
        return jdbcTemplate.query("select id,owner_id,name,species,breed,gender,birth_date,weight_kg,microchip from pet where owner_id=? order by id desc",
                (rs, i) -> {
                    Pet p = new Pet();
                    p.setId(rs.getLong("id"));
                    p.setOwnerId(rs.getLong("owner_id"));
                    p.setName(rs.getString("name"));
                    p.setSpecies(rs.getString("species"));
                    p.setBreed(rs.getString("breed"));
                    p.setGender(rs.getString("gender"));
                    p.setBirthDate(rs.getDate("birth_date") == null ? null : rs.getDate("birth_date").toLocalDate());
                    p.setWeightKg(rs.getObject("weight_kg") == null ? null : rs.getDouble("weight_kg"));
                    p.setMicrochip(rs.getString("microchip"));
                    return p;
                }, ownerId);
    }
}
