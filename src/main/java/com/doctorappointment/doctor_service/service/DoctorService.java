package com.doctorappointment.doctor_service.service;

import com.doctorappointment.doctor_service.dto.DoctorModel;
import com.doctorappointment.doctor_service.dto.DoctorRequest;
import com.doctorappointment.doctor_service.exception.*;
import com.doctorappointment.doctor_service.repository.DoctorRepoInterface;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;
@Singleton
@Slf4j
public class DoctorService {
    private final DoctorRepoInterface doctorRepo;

    public DoctorService(DoctorRepoInterface doctorRepo) {
        this.doctorRepo = doctorRepo;
    }

    //register doctor
    public DoctorModel addDoctor(DoctorRequest doctor) {
        if (doctorRepo.existsDoctorByEmail(doctor.email())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
//        if(doctor.password().length() < 8) {
//            throw new InvalidPasswordException("Password too short");
//        }
        String hashedPassword = BCrypt.hashpw(doctor.password(), BCrypt.gensalt());
        DoctorModel model = DoctorModel.builder()
                .doctorId(UUID.randomUUID())
                .firstName(doctor.firstName())
                .lastName(doctor.lastName())
                .email(doctor.email())
                .password(hashedPassword)
                .phoneNumber(doctor.phoneNumber())
                .address(doctor.address())
                .specialization(doctor.specialization())
                .clinicAddress(doctor.clinicAddress())
                .latitude(doctor.latitude())
                .longitude(doctor.longitude())
                .isDeleted(false)
                .build();
        log.info("Doctor with email {} register successfully", doctor.email());
        return doctorRepo.addDoctor(model);
    }

    //get doctor by id
    public DoctorModel getDoctorById(UUID id) {
        if (id == null) {
            throw new DoctorIdNotFoundException("Doctor id is required");
        }
        DoctorModel doctor = doctorRepo.getDoctorById(id);
        if (doctor == null) {
            throw new DoctorIdNotFoundException("Doctor not found with id " + id);
        }
        if (doctor.isDeleted()) {
            throw new DoctorIdNotFoundException("Doctor account is deactivated");
        }
        log.info("Doctor with id {} retrieved successfully", id);
        return doctor;
    }

    //get doctor by email
    public DoctorModel getDoctorByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new DoctorEmailNotFoundException(" Doctor email is required");
        }
        DoctorModel doctor = doctorRepo.getDoctorByEmail(email);
        if (doctor == null) {
            throw new DoctorEmailNotFoundException("Doctor not found with email " + email);
        }
        if (doctor.isDeleted()) {
            throw new DoctorEmailNotFoundException("Doctor account is deactivated");
        }
        log.info("Doctor with email {} retrieved successfully", email);
        return doctor;
    }

    //update doctor by id
    public DoctorModel updateDoctor(DoctorModel doctor) {
        if (doctor.doctorId() == null) {
            throw new DoctorIdNotFoundException("Doctor id is required");
        }
        DoctorModel existing = doctorRepo.getDoctorById(doctor.doctorId());
        if (existing == null) {
            throw new DoctorIdNotFoundException("Doctor not found with id " + doctor.doctorId());
        }
        if (existing.isDeleted()) {
            throw new DoctorIdNotFoundException("Doctor account is deactivated");
        }
        DoctorModel updated = DoctorModel.builder()
                .doctorId(existing.doctorId())
                .firstName(existing.firstName())
                .lastName(existing.lastName())
                .phoneNumber(existing.phoneNumber())
                .address(existing.address())
                .specialization(existing.specialization())
                .clinicAddress(existing.clinicAddress())
                .latitude(existing.latitude())
                .longitude(existing.longitude())
                .build();
        log.info("Updating doctor with ID {} ", doctor.doctorId());
        return doctorRepo.updateDoctor(updated);
    }

    //delete doctor by ID
    public void deleteDoctorById(UUID id) {
        if (id == null) {
            throw new DoctorIdNotFoundException("Doctor id is required");
        }
        DoctorModel doctor = doctorRepo.getDoctorById(id);
        if (doctor == null) {
            throw new DoctorIdNotFoundException("Doctor not found with id " + id);
        }
        if (doctor.isDeleted()) {
            throw new DoctorIdNotFoundException("Doctor account is deactivated");
        }
        log.info("Doctor with id {} deleted successfully", id);
        doctorRepo.deleteDoctorById(id);
    }

    //login
    public DoctorModel login(String email, String password) {
        if(email == null || email.isEmpty() || password == null) {
            throw new EmailPasswordRequiredException("Email or password is required");
        }
        DoctorModel doctor = doctorRepo.getDoctorByEmail(email);
        if (doctor == null) {
           throw new EmailPasswordRequiredException("Invalid email or password");
        }
        if (doctor.isDeleted()) {
            throw new DoctorIdNotFoundException("Doctor account is deactivated");
        }
        if(!BCrypt.checkpw(password, doctor.password())) {
            throw new InvalidPasswordException("Invalid password");
        }
        log.info("Logged in doctor with email {} successfully", email);
        return doctor;
    }
}
