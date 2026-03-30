package com.doctorappointment.config;

import com.datastax.oss.driver.api.core.CqlSession;
import jakarta.inject.Singleton;

import java.net.InetSocketAddress;

@Singleton
public class ScyllaDbConfig {
    private final CqlSession session;
    public ScyllaDbConfig(CqlSession session) {
        this.session = session;
    }
    public ScyllaDbConfig() {
        this.session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("localhost",9042))
                .withLocalDatacenter("datacenter1")
                .withKeyspace("doctor_appointment")
                .build();
    }

    public CqlSession getSession() {
        return session;
    }
}
