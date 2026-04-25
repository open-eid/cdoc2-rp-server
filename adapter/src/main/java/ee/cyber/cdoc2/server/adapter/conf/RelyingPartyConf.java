package ee.cyber.cdoc2.server.adapter.conf;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelyingPartyConf {
    private final String name;
    private final UUID uuid;
    private final CertificateLevel certificateLevel;
    private final String schemeName;

    @ConfigurationProperties(prefix = "app.rp")
    public record AppProperties(
        String name,
        String uuid,
        @DefaultValue("QUALIFIED") String certificateLevel,
        @DefaultValue("smart-id-demo") String schemeName
    ) {
    }

    public RelyingPartyConf(AppProperties props) {
        this.name = props.name;
        this.uuid = UUID.fromString(props.uuid);
        this.certificateLevel = CertificateLevel.valueOf(props.certificateLevel);
        this.schemeName = props.schemeName;
    }

    public String getName() {
        return this.name;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public CertificateLevel getCertificateLevel() {
        return this.certificateLevel;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public enum CertificateLevel {
        ADVANCED,
        QUALIFIED
    }
}
