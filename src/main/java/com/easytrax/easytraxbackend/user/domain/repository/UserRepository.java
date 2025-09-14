package com.easytrax.easytraxbackend.user.domain.repository;

import com.easytrax.easytraxbackend.global.oauth.domain.SocialProvider;
import com.easytrax.easytraxbackend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findBySocialProviderAndOauthId(SocialProvider socialProvider, String oauthId);

}
