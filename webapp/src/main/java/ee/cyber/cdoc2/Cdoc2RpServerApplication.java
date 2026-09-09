package ee.cyber.cdoc2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import ee.cyber.cdoc2.server.config.MonitoringUtil;

@SpringBootApplication()
@ConfigurationPropertiesScan
@EnableScheduling
public final class Cdoc2RpServerApplication {
    private Cdoc2RpServerApplication() {

    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Cdoc2RpServerApplication.class);
        // capture startup events for startup actuator endpoint
        app.setApplicationStartup(MonitoringUtil.getApplicationStartupInfo());
        app.run(args);
    }
}
