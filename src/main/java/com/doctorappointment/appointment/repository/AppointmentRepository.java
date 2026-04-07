package com.doctorappointment.appointment.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.doctorappointment.appointment.constant.AppointmentQuery;
import com.doctorappointment.appointment.constant.AppointmentSchema;
import com.doctorappointment.appointment.dto.AppointmentModel;
import com.doctorappointment.config.ScyllaDbConfig;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Singleton
class AppointmentRepository implements AppointmentRepoInterface {
    private final CqlSession cqlSession;
    //main table
    private final PreparedStatement insertDoctor;
    private final PreparedStatement findById;
    private final PreparedStatement updateStatus;
    //appointment by doctor table
    private final PreparedStatement insertByDoctor;
    private final PreparedStatement findByDoctor;
    private final PreparedStatement countByDoctorDate;
    private final PreparedStatement updateStatusByDoctor;
    //appointment by patient table
    private final PreparedStatement insertByPatient;
    private final PreparedStatement findByPatient;
    private final PreparedStatement updateStatusByPatient;

    AppointmentRepository(ScyllaDbConfig config) {
        this.cqlSession = config.getSession();
        this.insertDoctor = cqlSession.prepare(AppointmentQuery.INSERT);
        this.findById = cqlSession.prepare(AppointmentQuery.FIND_BY_ID);
        this.updateStatus = cqlSession.prepare(AppointmentQuery.UPDATE_STATUS);
        this.insertByDoctor = cqlSession.prepare(AppointmentQuery.INSERT_BY_DOCTOR);
        this.findByDoctor = cqlSession.prepare(AppointmentQuery.FIND_BY_DOCTOR);
        this.countByDoctorDate = cqlSession.prepare(AppointmentQuery.COUNT_BY_DOCTOR_DATE);
        this.updateStatusByDoctor = cqlSession.prepare(AppointmentQuery.UPDATE_STATUS_BY_DOCTOR);
        this.insertByPatient = cqlSession.prepare(AppointmentQuery.INSERT_BY_PATIENT);
        this.findByPatient = cqlSession.prepare(AppointmentQuery.FIND_BY_PATIENT);
        this.updateStatusByPatient = cqlSession.prepare(AppointmentQuery.UPDATE_STATUS_BY_PATIENT);
    }

    @Override
    public AppointmentModel saveAppointment(AppointmentModel appointment) {
        cqlSession.execute(insertDoctor.bind(
                appointment.appointmentId(),
                appointment.patientId(),
                appointment.doctorId(),
                appointment.appointment_date(),
                appointment.status(),
                appointment.notes(),
                appointment.reason(),
                appointment.cancelledBy(),
                appointment.createdAt()
        ));
        cqlSession.execute(insertByDoctor.bind(
                appointment.doctorId(),
                appointment.appointment_date(),
                appointment.appointmentId(),
                appointment.status()
        ));
        cqlSession.execute(insertByPatient.bind(
                appointment.patientId(),
                appointment.appointmentId(),
                appointment.appointment_date(),
                appointment.status()
        ));
        log.info(" Appointment saved : {}", appointment.appointmentId());
        return appointment;
    }

    @Override
    public AppointmentModel getAppointmentById(UUID appointmentId) {
        BoundStatement bs = findById.bind(appointmentId);
        Row row = cqlSession.execute(bs).one();
        if (row == null) {
            return null;
        }
        return mapRow(row);
    }

    @Override
    public void updateStatus(UUID appointmentId, String status, String reason, String cancelledBy) {
        AppointmentModel existingModel = getAppointmentById(appointmentId);
        if (existingModel == null) {
            return;
        }
        //update main table
        cqlSession.execute(updateStatus.bind(
                status,
                reason,
                cancelledBy,
                appointmentId.toString()
        ));
        //sync with appoints_by_doctor table
        cqlSession.execute(updateStatusByPatient.bind(
                status,
                existingModel.doctorId(),
                existingModel.appointment_date(),
                appointmentId.toString()
        ));
        //sync with appointments_by_patient
        cqlSession.execute(updateStatusByDoctor.bind(
                status,
                existingModel.doctorId(),
                appointmentId.toString()
        ));
        log.info(" Appointment status saved : {} -> {}", appointmentId, status);
    }

    @Override
    public List<AppointmentModel> getAppointmentByDoctor(UUID doctorId, String appointment_date) {
        BoundStatement bs = findByDoctor.bind(doctorId, appointment_date);
        ResultSet rs = cqlSession.execute(bs);
        List<AppointmentModel> appointments = new ArrayList<>();
        for (Row row : rs) {
            appointments.add(mapSummaryRow(row,
                    row.getUuid(AppointmentSchema.DOCTOR_ID), null));
        }
        return appointments;
    }

    @Override
    public List<AppointmentModel> getAppointmentByPatient(UUID patientId) {
        BoundStatement bs = findByPatient.bind(patientId);
        ResultSet rs = cqlSession.execute(bs);
        List<AppointmentModel> appointments = new ArrayList<>();
        for (Row row : rs) {
            appointments.add(mapSummaryRow(row, null, row.getUuid(AppointmentSchema.PATIENT_ID)));
        }
        return appointments;
    }

    @Override
    public long countByDoctorAndDate(UUID doctorId, String appointment_date) {
        BoundStatement bs = countByDoctorDate.bind(doctorId, appointment_date);
        Row row = cqlSession.execute(bs).one();
        return row == null ? 0 : row.getLong(0);

    }

    //helper method
    private AppointmentModel mapRow(Row row) {
        return AppointmentModel.builder()
                .appointmentId(row.getUuid(AppointmentSchema.APPOINTMENT_ID))
                .patientId(row.getUuid(AppointmentSchema.PATIENT_ID))
                .doctorId(row.getUuid(AppointmentSchema.DOCTOR_ID))
                .appointment_date(row.getString(AppointmentSchema.APPOINTMENT_DATE))
                .status(row.getString(AppointmentSchema.STATUS))
                .notes(row.getString(AppointmentSchema.NOTES))
                .reason(row.getString(AppointmentSchema.REASON))
                .cancelledBy(row.getString(AppointmentSchema.CANCELLED_BY))
                .createdAt(row.getInstant(AppointmentSchema.CREATED_AT))
                .build();
    }

    //for look up table
    private AppointmentModel mapSummaryRow(Row row,
                                           UUID doctorId,
                                           UUID patientId) {
        return AppointmentModel.builder()
                .appointmentId(row.getUuid(AppointmentSchema.APPOINTMENT_ID))
                .patientId(patientId)
                .doctorId(doctorId)
                .appointment_date(row.getString(AppointmentSchema.APPOINTMENT_DATE))
                .status(row.getString(AppointmentSchema.STATUS))
                .notes("")
                .reason("")
                .cancelledBy("")
                .createdAt(null)
                .build();
    }
}
