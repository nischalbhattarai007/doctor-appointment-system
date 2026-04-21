package com.doctorappointment.doctor.repository;

import com.doctorappointment.doctor.dto.DoctorScheduleModel;

import java.util.UUID;

public interface ScheduleRepoInterface {
    DoctorScheduleModel saveSchedule(DoctorScheduleModel schedule);
    DoctorScheduleModel getScheduleByDoctorId(UUID doctor_id);
}
