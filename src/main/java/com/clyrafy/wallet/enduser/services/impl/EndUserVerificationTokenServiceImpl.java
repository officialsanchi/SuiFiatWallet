package com.clyrafy.wallet.enduser.services.impl;

import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.enduser.data.models.EndUserVerificationToken;
import com.clyrafy.wallet.enduser.data.repositories.EndUserVerificationTokenRepository;
import com.clyrafy.wallet.enduser.services.EndUserVerificationTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class EndUserVerificationTokenServiceImpl implements EndUserVerificationTokenService {
    private final EndUserVerificationTokenRepository tokenRepository;

    public EndUserVerificationTokenServiceImpl(EndUserVerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }


    public String generateToken(EndUser endUser) {
        return getString(endUser, tokenRepository);
    }

    public static String getString(EndUser endUser, EndUserVerificationTokenRepository tokenRepository) {
        String token = UUID.randomUUID().toString();
        EndUserVerificationToken verificationToken = new EndUserVerificationToken();
        verificationToken.setEndUser(endUser);
        verificationToken.setToken(token);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));

        tokenRepository.deleteAllByEndUserId(endUser.getId());
        tokenRepository.save(verificationToken);

        return token;
    }

    public Optional<EndUser> validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(EndUserVerificationToken::getEndUser);
    }
}
