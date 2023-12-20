package com.hongxeob.motticle.image.application;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

	List<String> add(List<MultipartFile> files) throws IOException;

	void delete(String request);
}
