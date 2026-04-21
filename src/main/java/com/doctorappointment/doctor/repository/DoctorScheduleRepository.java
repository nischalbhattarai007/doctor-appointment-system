package com.doctorappointment.doctor.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.doctorappointment.config.ScyllaDbConfig;
import com.doctorappointment.doctor.constant.DoctorQuery;
import com.doctorappointment.doctor.dto.DoctorScheduleModel;
import com.doctorappointment.doctor.enums.Day;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Singleton
 class DoctorScheduleRepository implements ScheduleRepoInterface {
    private final CqlSession session;
    private final PreparedStatement upsert;
    private final PreparedStatement findByDoctorId;

    public DoctorScheduleRepository(ScyllaDbConfig config) {
        this.session=config.getSession();
        this.upsert=session.prepare(DoctorQuery.INSERT_INTO_DOCTOR_SCHEDULE);
        this.findByDoctorId=session.prepare(DoctorQuery.FIND_BY_ID);
    }

    @Override
    public DoctorScheduleModel saveSchedule(DoctorScheduleModel schedule) {
        Set<String> daysAsStrings = schedule.working_days().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
       session.execute(upsert.bind(
               schedule.doctor_id(),
               daysAsStrings
       ));
       log.info("Schedule saved for doctor {}",schedule.doctor_id());
        return schedule;
    }

    @Override
    public DoctorScheduleModel getScheduleByDoctorId(UUID doctor_id) {
        Row row = session.execute(findByDoctorId.bind(doctor_id)).one();
        if(row==null){
            return null;
        }
        Set<String> rawDays=row.getSet("working_days",String.class);
        if(rawDays==null || rawDays.isEmpty()){
            return null;
        }
        Set<Day> days=rawDays.stream()
                .map(Day::valueOf)
                .collect(Collectors.toSet());
        return DoctorScheduleModel.builder()
                .doctor_id(row.getUuid("doctor_id"))
                .working_days(days)
                .build();
    }
}
