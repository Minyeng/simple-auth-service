package tipah_apps.product_service.restfull.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import tipah_apps.product_service.restfull.entity.User;
import tipah_apps.product_service.restfull.model.RegisterUserRequest;
import tipah_apps.product_service.restfull.model.UserResponse;
import tipah_apps.product_service.restfull.model.UserUpdateRequest;
import tipah_apps.product_service.restfull.model.WebResponse;
import tipah_apps.product_service.restfull.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;



@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(
        path = "/api/users",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> register(@RequestBody RegisterUserRequest request) {
        userService.register(request);
        return WebResponse.<String>builder().data("OK").build();
    }

    @GetMapping(
        path = "/api/users/current",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> get(User user) {
        UserResponse userResponse = userService.get(user);
        return WebResponse.<UserResponse>builder()
                .data(userResponse).build();
    }

    @PatchMapping(
        path = "/api/users/current",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> set(User user, @RequestBody UserUpdateRequest request) {
        UserResponse userResponse = userService.set(user, request);
        return WebResponse.<UserResponse>builder()
                .data(userResponse).build();
    }
    
}
