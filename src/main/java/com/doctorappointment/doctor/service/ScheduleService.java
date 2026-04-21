package com.doctorappointment.doctor.service;

import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.dto.DoctorScheduleModel;
import com.doctorappointment.doctor.enums.Day;
import com.doctorappointment.doctor.exception.DoctorIdNotFoundException;
import com.doctorappointment.doctor.exception.DoctorNotAvailableException;
import com.doctorappointment.doctor.exception.ScheduleNotFoundException;
import com.doctorappointment.doctor.exception.ValidationException;
import com.doctorappointment.doctor.repository.DoctorRepoInterface;
import com.doctorappointment.doctor.repository.ScheduleRepoInterface;
import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class ScheduleService {
    private final ScheduleRepoInterface scheduleRepo;
    private final DoctorRepoInterface doctorRepo;

    public ScheduleService(ScheduleRepoInterface scheduleRepo, DoctorRepoInterface doctorRepo) {
        this.scheduleRepo = scheduleRepo;
        this.doctorRepo = doctorRepo;
    }
    public DoctorScheduleModel setSchedule(UUID doctorId, Set<Day> workingDays) {
        validateDays(workingDays);
        DoctorModel doctor=doctorRepo.getDoctorById(doctorId);
        if(doctor==null || doctor.isDeleted()){
            throw new DoctorIdNotFoundException("Doctor Id not found" +  doctorId);
        }
        DoctorScheduleModel schedule=DoctorScheduleModel.builder()
                .doctor_id(doctorId)
                .working_days(workingDays)
                .build();
        return scheduleRepo.saveSchedule(schedule);
    }

    public DoctorScheduleModel getSchedule(UUID doctorId) {
        DoctorScheduleModel schedule=scheduleRepo.getScheduleByDoctorId(doctorId);
        if(schedule==null){
            throw new DoctorIdNotFoundException("Doctor Id not found" +  doctorId);
        }
        return schedule;
    }
    //called by appointment service before booking or rescheduling
    public void assertWorkingDays(UUID doctorId, String date) {
        DoctorScheduleModel schedule=scheduleRepo.getScheduleByDoctorId(doctorId);
        if (schedule==null){
           throw new ScheduleNotFoundException("Doctor has not set a schedule yet. Booking is not available");
        }
        LocalDate localDate = LocalDate.parse(date);
        Day requestedDay=Day.valueOf(localDate.getDayOfWeek().name());

        if(!schedule.working_days().contains(requestedDay)){
            String workingDays=schedule.working_days().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(","));
            throw new DoctorNotAvailableException( "Doctor does not work on " + requestedDay
                    + ". Working days: " + workingDays);
        }
    }
    private void validateDays(Set<Day> days) {
        if (days == null || days.isEmpty())
            throw new ValidationException("At least one working day is required");
    }

}
