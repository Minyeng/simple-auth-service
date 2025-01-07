package tipah_apps.product_service.restfull.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import tipah_apps.product_service.restfull.entity.User;
import tipah_apps.product_service.restfull.model.LoginUserRequest;
import tipah_apps.product_service.restfull.model.TokenResponse;
import tipah_apps.product_service.restfull.repository.UserRepository;

@Slf4j
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Validator validator;

    @Transactional
    public TokenResponse login(LoginUserRequest request) {
        Set<ConstraintViolation<LoginUserRequest>> constraintViolations = validator.validate(request);
        if(constraintViolations.size() != 0) {
            throw new ConstraintViolationException(constraintViolations);
        }

        User user = userRepository.findByusername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password wrong"));
        
        if(DigestUtils.md5DigestAsHex(request.getPassword().getBytes()).equals(user.getPassword())) {
            user.setToken(UUID.randomUUID().toString());
            user.setTokenExpiredAt(getNext30Days());
            userRepository.save(user);

            return TokenResponse.builder()
                    .token(user.getToken())
                    .tokenExpiredAt(user.getTokenExpiredAt())
                    .build();
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password wrong");
    }
    
    public void logout(User user){
        user.setToken(null);
        user.setTokenExpiredAt(null);

        userRepository.save(user);
    }

    private Long getNext30Days() {
        return System.currentTimeMillis()+(1000L*3600L*24L*30L);
    }
}
