package ee.cyber.cdoc2.server.adapter.conf;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;

@Configuration
public class RelyingPartyConfImpl implements RelyingPartyConf {
    private final String sidName;
    private final UUID sidUuid;
    private final String midName;
    private final UUID midUuid;

    @ConfigurationProperties(prefix = "app.rp")
    public record AppProperties(
        Sid sid,
        Mid mid
    ) {

        record Sid(String name, String uuid) {
        }

        record Mid(String name, String uuid) {
        }
    }

    public RelyingPartyConfImpl(AppProperties props) {
        this.sidName = props.sid.name;
        this.sidUuid = UUID.fromString(props.sid.uuid);
        this.midName = props.mid.name;
        this.midUuid = UUID.fromString(props.mid.uuid);
    }

    @Override
    public String getSidName() {
        return this.sidName;
    }

    @Override
    public UUID getSidUuid() {
        return this.sidUuid;
    }

    @Override
    public String getMidName() {
        return midName;
    }

    @Override
    public UUID getMidUuid() {
        return midUuid;
    }
}
