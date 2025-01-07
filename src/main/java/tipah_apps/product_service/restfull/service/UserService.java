package tipah_apps.product_service.restfull.service;

import java.util.Objects;
import java.util.Set;

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
import tipah_apps.product_service.restfull.model.RegisterUserRequest;
import tipah_apps.product_service.restfull.model.UserResponse;
import tipah_apps.product_service.restfull.model.UserUpdateRequest;
import tipah_apps.product_service.restfull.repository.UserRepository;

@Slf4j
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Validator validator;

    @Transactional
    public void register(RegisterUserRequest request) {
        Set<ConstraintViolation<RegisterUserRequest>> constraintViolations = validator.validate(request);
        if(constraintViolations.size() != 0) {
            throw new ConstraintViolationException(constraintViolations);
        }

        if(userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User alredy registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setPassword(DigestUtils.md5DigestAsHex(request.getPassword().getBytes()));

        userRepository.save(user);
    }

    public UserResponse get(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }

    @Transactional
    public UserResponse set(User user, UserUpdateRequest request) {
        Set<ConstraintViolation<UserUpdateRequest>> constraintViolations = validator.validate(request);
        if(constraintViolations.size() != 0) {
            throw new ConstraintViolationException(constraintViolations);
        }

        if(Objects.nonNull(request.getName())) {
            user.setName(request.getName());
        }
        
        if(Objects.nonNull(request.getPassword())) {
            String passwordHash = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());

            if(user.getPassword().equals(passwordHash)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be the same as the old password");
            }
            user.setPassword(passwordHash);
        }

        if(Objects.nonNull(request.getUsername())) {
            user.setUsername(request.getUsername());
        }

        userRepository.save(user);

        return UserResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }
}
