package com.doctorappointment.doctor.service;
import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.dto.DoctorRequest;
import com.doctorappointment.doctor.exception.*;
import com.doctorappointment.doctor.repository.DoctorRepoInterface;
import com.doctorappointment.doctor.repository.DoctorServiceInterface;
import com.doctorappointment.doctor.util.GeoCalculateDistance;
import com.doctorappointment.doctor.util.GeohashUtil;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Singleton
@Slf4j
public class DoctorService implements DoctorServiceInterface {
    private final DoctorRepoInterface doctorRepo;
    private final GeocodingService geocodingService;

    public DoctorService(DoctorRepoInterface doctorRepo, GeocodingService geocodingService) {
        this.doctorRepo = doctorRepo;
        this.geocodingService = geocodingService;
    }

    //register doctor
    @Override
    public DoctorModel addDoctor(DoctorRequest doctor) {
        ValidateDoctor.validateName(doctor.firstName(), doctor.lastName());
        ValidateDoctor.validateEmail(doctor.email());
        ValidateDoctor.validateCity(doctor.city());
        ValidateDoctor.validatePhone(doctor.phoneNumber());
        ValidateDoctor.validateSpecialization(doctor.specialization());
        ValidateDoctor.validateStreet(doctor.street());
        ValidateDoctor.validateArea(doctor.area());
        ValidateDoctor.validatePassword(doctor.password());
        if (doctorRepo.existsDoctorByEmail(doctor.email())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        // area + city, building form a unique location key
        String normalizedBuilding = normalizeBuilding(doctor.clinicBuilding());
        String normalizedArea = normalizeArea(doctor.area());
        String normalizedCity = normalizeCity(doctor.city());
        if (!normalizedArea.isBlank()
                && !normalizedCity.isBlank()
                && !normalizedBuilding.isBlank()
                && doctorRepo.existsByClinicAddressAndBuilding(
                        normalizedArea, normalizedCity, normalizedBuilding)) {
            throw new ClinicLocationValidataionException(
                    "A clinic already exists at '" + doctor.area() + ", " + doctor.city() +
                            " with " + doctor.clinicBuilding() + "'. " +
                            "If you practice at the same location, contact support.");
        }
        String hashedPassword = BCrypt.hashpw(doctor.password(), BCrypt.gensalt());
        //get coordinates from clinic address
        double[] coordinates = geocodingService.getCoordinates
                (
                        doctor.street(),
                        doctor.area(),
                        doctor.city());

        double latitude = coordinates[0];
        double longitude = coordinates[1];
        String geoHash= GeohashUtil.encode(latitude, longitude);
        log.info("Doctor registered at lat={}, lon={} -> geohash={}", latitude, longitude, geoHash);
        DoctorModel model = DoctorModel.builder()
                .doctorId(UUID.randomUUID())
                .firstName(doctor.firstName())
                .lastName(doctor.lastName())
                .email(doctor.email())
                .password(hashedPassword)
                .phoneNumber(doctor.phoneNumber())
                .address(doctor.address())
                .specialization(doctor.specialization())
                .city(doctor.city())
                .street(doctor.street())
                .area(doctor.area())
                .dailyLimit(10)
                .isDeleted(false)
                .latitude(latitude)
                .longitude(longitude)
                .clinicName(doctor.clinicName())
                .clinicBuilding(normalizedBuilding)
                .geoHash(geoHash)
                .build();

        log.info("Doctor with email {} register successfully", doctor.email());
        return doctorRepo.addDoctor(model);
    }

    //get doctor by id
    @Override
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
    @Override
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
    @Override
    public DoctorModel updateDoctor(DoctorModel doctor) {
        ValidateDoctor.validateEmail(doctor.email());
        ValidateDoctor.validatePhone(doctor.phoneNumber());
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

        // resolve effective clinic fields (new value if provided, else keep existing)
        String effectiveStreet = isBlank(doctor.street()) ? existing.street() : doctor.street();
        String effectiveArea = isBlank(doctor.area()) ? existing.area() : doctor.area();
        String effectiveCity = isBlank(doctor.city()) ? existing.city() : doctor.city();
        String effectiveBuilding = isBlank(doctor.clinicBuilding()) ? existing.clinicBuilding() : doctor.clinicBuilding();
        String effectiveName = isBlank(doctor.clinicName()) ? existing.clinicName() : doctor.clinicName();

        // check if any clinic location field actually changed
        boolean clinicChanged = !effectiveStreet.equals(existing.street())
                || !effectiveArea.equals(existing.area())
                || !effectiveCity.equals(existing.city());

        // declare lat/lon starting from existing values
        double latitude = existing.latitude();
        double longitude = existing.longitude();

        if (clinicChanged) {
            try {
                double[] coords = geocodingService.getCoordinates(effectiveStreet, effectiveArea, effectiveCity);
                latitude = coords[0];
                longitude = coords[1];
                log.info("Clinic changed -> re-geocoded: lat={}, lon={}", latitude, longitude);
            } catch (Exception e) {
                log.warn("Geocoding failed, keeping old coordinates", e);
            }
        }

        String newGeohash = GeohashUtil.encode(latitude, longitude);

        DoctorModel updated = DoctorModel.builder()
                .doctorId(existing.doctorId())
                .firstName(isBlank(doctor.firstName()) ? existing.firstName() : doctor.firstName())
                .lastName(isBlank(doctor.lastName()) ? existing.lastName() : doctor.lastName())
                .phoneNumber(isBlank(doctor.phoneNumber()) ? existing.phoneNumber() : doctor.phoneNumber())
                .address(isBlank(doctor.address()) ? existing.address() : doctor.address())
                .email(isBlank(doctor.email()) ? existing.email() : doctor.email())
                .specialization(isBlank(doctor.specialization()) ? existing.specialization() : doctor.specialization())
                .street(effectiveStreet)
                .area(effectiveArea)
                .city(effectiveCity)
                .clinicName(effectiveName)
                .clinicBuilding(effectiveBuilding)
                .latitude(latitude)
                .longitude(longitude)
                .dailyLimit(doctor.dailyLimit() == 0 ? existing.dailyLimit() : doctor.dailyLimit())
                .geoHash(newGeohash)
                .build();

        log.info("Updating doctor with ID {}", doctor.doctorId());
        return doctorRepo.updateDoctor(updated, existing.geoHash());
    }

    //delete doctor by ID
    @Override
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
    @Override
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
    @Override
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

    //get doctors by location
    @Override
    public List<DoctorModel> getDoctorsByLocation(double latitude,
                                                  double longitude,
                                                  double radiusKm,
                                                  int limit) {
        String centerHash=GeohashUtil.encode(latitude,longitude);
        Set<String> prefixes=GeohashUtil.getNeighborAndSelf(centerHash);
        return doctorRepo.findDoctorsByGeohashPrefixes(prefixes).stream()
                .filter(d->!d.isDeleted())
                .filter(d-> GeoCalculateDistance.calculateDistance(
                        latitude,longitude,d.latitude(),d.longitude())<=radiusKm)
                .sorted((a,b)->Double.compare(
                        GeoCalculateDistance.calculateDistance(latitude,longitude,a.latitude(),a.longitude()),
                        GeoCalculateDistance.calculateDistance(latitude,longitude,b.latitude(),b.longitude())))
                .limit(limit)
                .toList();
    }

    //get nearest doctor
    @Override
    public List<DoctorModel> getNearestDoctors(double lat1, double lon1, double radiusKm, int limit) {
        return getDoctorsByLocation(lat1, lon1, radiusKm, limit);
    }

    //get all doctors
    @Override
    public List<DoctorModel> getAllDoctors() {
        return doctorRepo.getAllDoctors();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    //add a private helper class
    private String normalizeBuilding(String clinicBuilding) {
        return normalize(clinicBuilding);
    }
    private String normalizeArea(String area) {
       return normalize(area);
    }
    private String normalizeCity(String city) {
       return normalize(city);
    }
    private String normalize(String input){
        if(input==null || input.isBlank()) return "";
        return input.trim()
                .toLowerCase()
                .replaceAll("\\s*,\\s*",",");
    }}
