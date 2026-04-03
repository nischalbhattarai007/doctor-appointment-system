package com.doctorappointment.doctor.service;

import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.dto.DoctorRequest;
import com.doctorappointment.doctor.exception.*;
import com.doctorappointment.doctor.repository.DoctorRepoInterface;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class DoctorService {
    private final DoctorRepoInterface doctorRepo;
    private final GeocodingService geocodingService;

    public DoctorService(DoctorRepoInterface doctorRepo, GeocodingService geocodingService) {
        this.doctorRepo = doctorRepo;
        this.geocodingService = geocodingService;
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
        //get coordinates from clinic address
        double[] coordinates = geocodingService.getCoordinates(doctor.clinicAddress());
        double latitude = coordinates[0];
        double longitude = coordinates[1];
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
                .latitude(latitude)
                .longitude(longitude)
                .dailyLimit(10)
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
                .dailyLimit(existing.dailyLimit())
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
        if (email == null || email.isEmpty() || password == null) {
            throw new EmailPasswordRequiredException("Email or password is required");
        }
        DoctorModel doctor = doctorRepo.getDoctorByEmail(email);
        if (doctor == null) {
            log.info("Doctor with email {} login successfully", email);
            throw new EmailPasswordRequiredException("Invalid email or password");
        }
        if (doctor.isDeleted()) {
            throw new DoctorIdNotFoundException("Doctor account is deactivated");
        }
        if (!BCrypt.checkpw(password, doctor.password())) {
            throw new InvalidPasswordException("Invalid password");
        }
        log.info("Logged in doctor with email {} successfully", email);
        return doctor;
    }

    //get doctor availability
    public DoctorModel getDoctorAvailability(UUID id) {
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
        log.info("Doctor with id {} availability successfully", id);
        return doctor;
    }

    //calculate distance using haversine formula
    public double calculateDistance(double lat1, double lon1,
                                     double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    //get doctors by location
    public List<DoctorModel> getDoctorsByLocation(double latitude,
                                                  double longitude,
                                                  double radiusKm,
                                                  int limit) {
        List<DoctorModel> all = doctorRepo.getAllDoctors();
        return all.stream()
                .filter(d -> !d.isDeleted())
                .filter(d -> calculateDistance(
                        latitude, longitude,
                        d.latitude(), d.longitude()) <= radiusKm)
                .sorted((a, b) -> Double.compare(
                        calculateDistance(latitude, longitude, a.latitude(), a.longitude()),
                        calculateDistance(latitude, longitude, b.latitude(), b.longitude())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    //get nearest doctor
    public List<DoctorModel> getNearestDoctors(double lat1, double lon1, double radiusKm, int limit) {
        return getDoctorsByLocation(lat1, lon1, radiusKm, limit);
    }
}
