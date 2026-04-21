package com.doctorappointment.doctor.dto;
import com.doctorappointment.doctor.enums.Day;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Introspected
@Serdeable
@Builder(toBuilder = true)
public record DoctorScheduleModel (
        UUID doctor_id,
        Set<Day> working_days)
{}
