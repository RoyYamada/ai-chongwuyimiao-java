package com.example.demo.pet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class VaccinationService {
    private final VaccineRepository vaccineRepository;
    private final VaccinationRepository vaccinationRepository;

    public VaccinationService(VaccineRepository vaccineRepository, VaccinationRepository vaccinationRepository) {
        this.vaccineRepository = vaccineRepository;
        this.vaccinationRepository = vaccinationRepository;
    }

    @Transactional
    public Long record(Vaccination v, Vaccine vaccineMeta) {
        Instant next = null;
        int dosesRequired = vaccineMeta.getDosesRequired() == null ? 1 : vaccineMeta.getDosesRequired();
        int intervalDays = vaccineMeta.getIntervalDays() == null ? 0 : vaccineMeta.getIntervalDays();
        int validMonths = vaccineMeta.getValidMonths() == null ? 12 : vaccineMeta.getValidMonths();
        Instant administered = v.getAdministeredAt() == null ? Instant.now() : v.getAdministeredAt();
        if (v.getDoseNumber() != null && v.getDoseNumber() < dosesRequired) {
            LocalDateTime ldt = LocalDateTime.ofInstant(administered, ZoneId.systemDefault()).plusDays(intervalDays);
            next = ldt.atZone(ZoneId.systemDefault()).toInstant();
        } else {
            LocalDateTime ldt = LocalDateTime.ofInstant(administered, ZoneId.systemDefault()).plusMonths(validMonths);
            next = ldt.atZone(ZoneId.systemDefault()).toInstant();
        }
        v.setAdministeredAt(administered);
        v.setNextDueAt(next);
        v.setStatus("RECORDED");
        return vaccinationRepository.create(v);
    }
}
