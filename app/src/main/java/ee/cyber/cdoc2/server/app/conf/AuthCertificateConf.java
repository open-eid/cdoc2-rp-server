package ee.cyber.cdoc2.server.app.conf;

public interface AuthCertificateConf {
    boolean isRevocationChecksEnabled();
    boolean isSignCertForbidden();
}
