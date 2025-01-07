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
import tipah_apps.product_service.restfull.model.RegisterUserRequest;
import tipah_apps.product_service.restfull.model.UserResponse;
import tipah_apps.product_service.restfull.model.UserUpdateRequest;
import tipah_apps.product_service.restfull.model.WebResponse;
import tipah_apps.product_service.restfull.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
// import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

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
	void successRegister() throws Exception {
		RegisterUserRequest registerUserRequest = new RegisterUserRequest();
		registerUserRequest.setUsername("test");
		registerUserRequest.setName("Test One");
		registerUserRequest.setPassword("1234");

		mockMvc.perform(
			post("/api/users")
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(registerUserRequest))
		).andExpectAll(
			status().isOk()
		).andDo(result -> {
			WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

			assertEquals("OK", response.getData());
		});
	}
	
	@Test
	void badRegister() throws Exception {
		RegisterUserRequest registerUserRequest = new RegisterUserRequest();
		registerUserRequest.setUsername("");
		registerUserRequest.setName("");
		registerUserRequest.setPassword("");

		mockMvc.perform(
			post("/api/users")
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(registerUserRequest))
		).andExpectAll(
			status().isBadRequest()
		).andDo(result -> {
			WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

			assertNotNull(response.getErrors());
		});
	}

	@Test
	void duplicateRegister() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setName("Test One");
		user.setPassword(DigestUtils.md5DigestAsHex("1234".getBytes()));
		userRepository.save(user);

		RegisterUserRequest registerUserRequest = new RegisterUserRequest();
		registerUserRequest.setUsername("test");
		registerUserRequest.setName("Test Two");
		registerUserRequest.setPassword("4321");

		mockMvc.perform(
			post("/api/users")
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(registerUserRequest))
		).andExpectAll(
			status().isBadRequest()
		).andDo(result -> {
			WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

			assertNotNull(response.getErrors());
		});
	}
	
	@Test
	void tokenInvalid() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setName("Test One");
		user.setPassword(DigestUtils.md5DigestAsHex("1234".getBytes()));
		user.setToken("token");
		user.setTokenExpiredAt(System.currentTimeMillis()*2);
		userRepository.save(user);

		mockMvc.perform(
			get("/api/users/current")
					.accept(MediaType.APPLICATION_JSON)
					.header("X-API-TOKEN", "tokenium")
		).andExpectAll(
			status().isUnauthorized()
		).andDo(result -> {
			WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

			assertNotNull(response.getErrors());
		});
	}
	
	@Test
	void tokenExpired() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setName("Test One");
		user.setPassword(DigestUtils.md5DigestAsHex("1234".getBytes()));
		user.setToken("token");
		user.setTokenExpiredAt(System.currentTimeMillis()-10000);
		userRepository.save(user);

		mockMvc.perform(
			get("/api/users/current")
					.accept(MediaType.APPLICATION_JSON)
					.header("X-API-TOKEN", "token")
		).andExpectAll(
			status().isUnauthorized()
		).andDo(result -> {
			WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

			assertNotNull(response.getErrors());
		});
	}
	
	@Test
	void tokenValid() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setName("Test One");
		user.setPassword(DigestUtils.md5DigestAsHex("1234".getBytes()));
		user.setToken("token");
		user.setTokenExpiredAt(System.currentTimeMillis()*2);
		userRepository.save(user);

		mockMvc.perform(
			get("/api/users/current")
					.accept(MediaType.APPLICATION_JSON)
					.header("X-API-TOKEN", "token")
		).andExpectAll(
			status().isOk()
		).andDo(result -> {
			WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>(){});

			assertNotNull(response.getData());
			assertEquals(user.getName(), response.getData().getName());
			assertEquals(user.getUsername(), response.getData().getUsername());
		});
	}
	
	@Test
	void updateFailed() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setName("Test One");
		user.setPassword(DigestUtils.md5DigestAsHex("1234".getBytes()));
		user.setToken("token");
		user.setTokenExpiredAt(System.currentTimeMillis()*2);
		userRepository.save(user);

		UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
		userUpdateRequest.setUsername("testi");
		userUpdateRequest.setName("Test Two");
		userUpdateRequest.setPassword("1234");

		mockMvc.perform(
			patch("/api/users/current")
					.accept(MediaType.APPLICATION_JSON)
					.header("X-API-TOKEN", "token")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(userUpdateRequest))
		).andExpectAll(
			status().isBadRequest()
		).andDo(result -> {
			WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>(){});

			assertNotNull(response.getErrors());
		});
	}
	
	@Test
	void updateSuccess() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setName("Test One");
		user.setPassword(DigestUtils.md5DigestAsHex("1234".getBytes()));
		user.setToken("token");
		user.setTokenExpiredAt(System.currentTimeMillis()*2);
		userRepository.save(user);

		UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
		userUpdateRequest.setUsername("testi");
		userUpdateRequest.setName("Test Two");
		userUpdateRequest.setPassword("1235");

		mockMvc.perform(
			patch("/api/users/current")
					.accept(MediaType.APPLICATION_JSON)
					.header("X-API-TOKEN", "token")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(userUpdateRequest))
		).andExpectAll(
			status().isOk()
		).andDo(result -> {
			WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>(){});

			assertNull(response.getErrors());
			assertEquals("Test Two", response.getData().getName());
			assertEquals("testi", response.getData().getUsername());
		});
	}

}
