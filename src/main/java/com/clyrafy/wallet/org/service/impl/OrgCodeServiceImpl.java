package com.clyrafy.wallet.org.service.impl;

import com.clyrafy.wallet.org.data.models.OrgUniqueCode;
import com.clyrafy.wallet.org.data.repositories.OrgUniqueCodeRepository;
import com.clyrafy.wallet.org.service.OrgCodeService;
import com.clyrafy.wallet.org.utils.OrgIdCodeGeneratorConf;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrgCodeServiceImpl implements OrgCodeService {

    private final OrgUniqueCodeRepository codeRepository;
    private final OrgIdCodeGeneratorConf config;

    private final Lock lock = new ReentrantLock();
    private final SecureRandom secureRandom = new SecureRandom();


    @Transactional
//    @Retryable(value = { DataIntegrityViolationException.class }, maxAttempts = 3)
    public String generateUniqueOrgCode(String orgId) {
        lock.lock();
        try {
            Optional<OrgUniqueCode> availableCode = codeRepository.findNextAvailableCode(orgId);
            if (availableCode.isPresent()) {
                markCodeAsUsed(availableCode.get().getCode(), orgId);
                return availableCode.get().getCode();
            }
            return generateNewUniqueCode(orgId);
        } finally {
            lock.unlock();
        }
    }

    private String generateNewUniqueCode(String orgId) {
        int attempts = 0;

        while (attempts < config.getMaxAttempts()) {
            String code = generateRandomCode();

            if (codeRepository.existsByCodeAndOrgId(code, orgId)) {
                attempts++;
                continue;
            }

            try {
                OrgUniqueCode uniqueCode = new OrgUniqueCode(code, orgId);
                uniqueCode.setStatus(OrgUniqueCode.CodeStatus.USED);
                uniqueCode.setUsedAt(java.time.LocalDateTime.now());

                codeRepository.save(uniqueCode);
                log.info("Generated new unique code: {} for org: {}", code, orgId);
                return code;

            } catch (DataIntegrityViolationException e) {
                attempts++;
                log.debug("Code collision for: {} (attempt {})", code, attempts);
            }
        }

        throw new RuntimeException("Failed to generate unique code after " + config.getMaxAttempts() + " attempts for org: " + orgId);
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(config.getCodeLength());
        for (int i = 0; i < config.getCodeLength(); i++) {
            int index = secureRandom.nextInt(config.getCharacters().length());
            sb.append(config.getCharacters().charAt(index));
        }
        return sb.toString();
    }

    @Transactional
    public boolean markCodeAsUsed(String code, String orgId) {
        int updated = codeRepository.markCodeAsUsed(code, orgId);
        return updated > 0;
    }

    @Transactional
    public boolean isCodeAvailable(String code, String orgId) {
        return !codeRepository.existsByCodeAndOrgId(code, orgId);
    }


    @Transactional
    public void preGenerateOrgCodes(String orgId, int batchSize) {
        for (int i = 0; i < batchSize; i++) {
            String code;
            do {
                code = generateRandomCode();
            } while (codeRepository.existsByCodeAndOrgId(code, orgId));

            OrgUniqueCode uniqueCode = new OrgUniqueCode(code, orgId);
            codeRepository.save(uniqueCode);
        }
        log.info("Pre-generated {} codes for org: {}", batchSize, orgId);
    }


    public String getFullOrgIdentifier(String orgName, String code) {
        return orgName + "-" + code;
    }
}