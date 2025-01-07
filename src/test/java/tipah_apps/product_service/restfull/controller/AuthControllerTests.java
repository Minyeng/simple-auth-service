package tipah_apps.product_service.restfull.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.DigestUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tipah_apps.product_service.restfull.entity.User;
import tipah_apps.product_service.restfull.model.LoginUserRequest;
import tipah_apps.product_service.restfull.model.TokenResponse;
import tipah_apps.product_service.restfull.model.WebResponse;
import tipah_apps.product_service.restfull.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
// import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	void loginFailedNotFound() throws Exception {
		LoginUserRequest loginUserRequest = new LoginUserRequest();
		loginUserRequest.setUsername("test");
		loginUserRequest.setPassword("1234");

		mockMvc.perform(
			post("/api/auth/login")
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(loginUserRequest))
		).andExpectAll(
			status().isUnauthorized()
		).andDo(result -> {
			WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

			assertNotNull(response.getErrors());
		});
	}
	
	@Test
	void loginFailedValidation() throws Exception {
		LoginUserRequest loginUserRequest = new LoginUserRequest();
		loginUserRequest.setUsername("");
		loginUserRequest.setPassword("");

		mockMvc.perform(
			post("/api/auth/login")
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(loginUserRequest))
		).andExpectAll(
			status().isBadRequest()
		).andDo(result -> {
			WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

			assertNotNull(response.getErrors());
		});
	}

	@Test
	void loginFailedWrongPassword() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setPassword(DigestUtils.md5DigestAsHex("1234".getBytes()));
		user.setName("Test One");
		userRepository.save(user);

		LoginUserRequest loginUserRequest = new LoginUserRequest();
		loginUserRequest.setUsername("tset");
		loginUserRequest.setPassword("4321");

		mockMvc.perform(
			post("/api/auth/login")
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(loginUserRequest))
		).andExpectAll(
			status().isUnauthorized()
		).andDo(result -> {
			WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

			assertNotNull(response.getErrors());
		});
	}

	@Test
	void loginSuccess() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setPassword(DigestUtils.md5DigestAsHex("1234".getBytes()));
		user.setName("Test One");
		userRepository.save(user);

		LoginUserRequest loginUserRequest = new LoginUserRequest();
		loginUserRequest.setUsername("test");
		loginUserRequest.setPassword("1234");

		mockMvc.perform(
			post("/api/auth/login")
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(loginUserRequest))
		).andExpectAll(
			status().isOk()
		).andDo(result -> {
			WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TokenResponse>>(){});

			assertNull(response.getErrors());
			assertNotNull(response.getData().getToken());
			assertNotNull(response.getData().getTokenExpiredAt());

			User userDb = userRepository.findByusername("test").orElse(null);
			assertNotNull(userDb);
			assertEquals(userDb.getToken(), response.getData().getToken());
			assertEquals(userDb.getTokenExpiredAt(), response.getData().getTokenExpiredAt());
		});
	}
	
	@Test
	void loginFailed() throws Exception {
		mockMvc.perform(
			delete("/api/auth/logout")
					.accept(MediaType.APPLICATION_JSON)
		).andExpectAll(
			status().isUnauthorized()
		).andDo(result -> {
			WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

			assertNotNull(response.getErrors());
		});
	}
	
	@Test
	void logutSuccess() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setPassword(DigestUtils.md5DigestAsHex("1234".getBytes()));
		user.setName("Test One");
		user.setToken("token");
		user.setTokenExpiredAt(System.currentTimeMillis()*2);
		userRepository.save(user);

		mockMvc.perform(
			delete("/api/auth/logout")
					.accept(MediaType.APPLICATION_JSON)
					.header("X-API-TOKEN", "token")
		).andExpectAll(
			status().isOk()
		).andDo(result -> {
			WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

			assertNull(response.getErrors());

			User userDb = userRepository.findByusername("test").orElse(null);
			assertNotNull(userDb);
			assertNull(userDb.getToken());
			assertNull(userDb.getTokenExpiredAt());
		});
	}
}
