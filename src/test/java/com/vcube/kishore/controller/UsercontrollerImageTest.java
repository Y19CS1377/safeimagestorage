package com.vcube.kishore.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.vcube.kishore.entity.Files;
import com.vcube.kishore.repositary.FilesRepository;
import com.vcube.kishore.service.FilesService;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsercontrollerImageTest {

	private FilesService filesService;
	private MockMvc mockMvc;
	private byte[] jpegData;
	private Map<Long, Files> storedFiles;

	@BeforeEach
	void setUp() throws Exception {
		storedFiles = new HashMap<>();
		FilesRepository repository = (FilesRepository) Proxy.newProxyInstance(
				FilesRepository.class.getClassLoader(),
				new Class<?>[] { FilesRepository.class },
				(proxy, method, args) -> {
					if ("findById".equals(method.getName())) {
						return Optional.ofNullable(storedFiles.get(args[0]));
					}
					throw new UnsupportedOperationException(method.getName());
				});
		filesService = new FilesService(repository);
		Usercontroller controller = new Usercontroller();
		ReflectionTestUtils.setField(controller, "fs", filesService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setViewResolvers((viewName, locale) -> (model, request, response) -> {
				})
				.build();

		BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		assertTrue(ImageIO.write(image, "jpg", output));
		jpegData = output.toByteArray();
	}

	@Test
	void servesRawImageBytesWithStoredMediaType() throws Exception {
		storedFiles.put(1L, new Files(1L, "photo.jpg", "image/jpeg", jpegData));

		mockMvc.perform(get("/file-images/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.IMAGE_JPEG))
				.andExpect(result -> assertArrayEquals(jpegData, result.getResponse().getContentAsByteArray()));
	}

	@Test
	void detectsImageTypeForLegacyUploads() throws Exception {
		storedFiles.put(2L, new Files(2L, "photo", "image", jpegData));

		mockMvc.perform(get("/file-images/2"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.IMAGE_JPEG))
				.andExpect(result -> assertArrayEquals(jpegData, result.getResponse().getContentAsByteArray()));
	}

	@Test
	void returnsNotFoundWhenFileDoesNotExist() throws Exception {
		mockMvc.perform(get("/file-images/3"))
				.andExpect(status().isNotFound());
	}

	@Test
	void staticImagePathIsNotTreatedAsAnUploadedImageId() throws Exception {
		mockMvc.perform(get("/images/home-img.avif"))
				.andExpect(status().isNotFound());
	}

	@Test
	void loginRedirectPathRendersLoginPage() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"));
	}
}
