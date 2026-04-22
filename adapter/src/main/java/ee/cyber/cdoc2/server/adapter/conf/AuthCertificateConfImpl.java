package ee.cyber.cdoc2.server.adapter.conf;

import ee.cyber.cdoc2.server.app.conf.AuthCertificateConf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthCertificateConfImpl implements AuthCertificateConf {
    private final boolean isRevocationChecksEnabled;
    private final boolean isSignCertForbidden;

    public AuthCertificateConfImpl(AuthCertificateConfigProperties props) {
        this.isRevocationChecksEnabled = props.revocationChecksEnabled;
        this.isSignCertForbidden = props.signCertForbidden;
    }

    @ConfigurationProperties(prefix = "app.cdoc2.auth-x5c")
    public record AuthCertificateConfigProperties(
        @DefaultValue("false") boolean revocationChecksEnabled,
        @DefaultValue("true") boolean signCertForbidden
    ) {
    }

    @Override
    public boolean isRevocationChecksEnabled() {
        return isRevocationChecksEnabled;
    }

    @Override
    public boolean isSignCertForbidden() {
        return isSignCertForbidden;
    }
}
