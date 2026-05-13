package ee.cyber.cdoc2.server.adapter.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionNonceCleanUpJob {

    private final SessionNonceRepository sessionNonceRepository;

    @Value("${session-nonce.expired.clean-up.delete-limit:1000}")
    private int deleteLimit;

    @Scheduled(cron = "${session-nonce.expired.clean-up.cron}")
    public void deleteExpiredSessionNonces() {
        log.debug("Starting expired session nonce clean-up");
        int deleted = sessionNonceRepository.deleteExpiredNonces(deleteLimit);
        log.debug("Deleted {} expired session nonce(s)", deleted);
    }
}
