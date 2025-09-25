package com.clyrafy.wallet.org.service.impl;

import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.enduser.data.repositories.EndUserVerificationTokenRepository;
import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.data.models.VerificationToken;
import com.clyrafy.wallet.org.data.repositories.VerificationTokenRepository;
import com.clyrafy.wallet.org.enums.OrgStatus;
import com.clyrafy.wallet.org.service.VerificationTokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.clyrafy.wallet.enduser.services.impl.EndUserVerificationTokenServiceImpl.getString;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final EndUserVerificationTokenRepository endUserVerificationTokenRepository;

    public VerificationTokenServiceImpl(VerificationTokenRepository verificationTokenRepository, EndUserVerificationTokenRepository endUserVerificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.endUserVerificationTokenRepository = endUserVerificationTokenRepository;
    }

    @Override
    public String generateToken(User user) {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User cannot be null or must have a valid ID");
        }

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));

        verificationTokenRepository.deleteAllByUserId(user.getId());
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    @Override
    public String generateEndUserToken(EndUser user) {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User cannot be null or must have a valid ID");
        }

        return getString(user, endUserVerificationTokenRepository);
    }

    @Override
    public OrgStatus validateToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .map(verificationToken -> {
                    if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return OrgStatus.EXPIRED;
                    }
                    return OrgStatus.ACTIVE;
                })
                .orElse(OrgStatus.INVALID);
    }

    @Override
    public Optional<OrgStatus> validateEndUserToken(String token) {
        return Optional.empty();
    }
}