package com.patient.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.patient.kafka.PatientEventProducer;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public PatientEventProducer mockPatientEventProducer() {
        return Mockito.mock(PatientEventProducer.class);
    }
}
