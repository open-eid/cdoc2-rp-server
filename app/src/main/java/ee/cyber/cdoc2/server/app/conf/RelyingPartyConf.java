package ee.cyber.cdoc2.server.app.conf;

import java.util.UUID;

public interface RelyingPartyConf {
    String getSidName();

    UUID getSidUuid();

    String getMidName();

    UUID getMidUuid();
}
