package com.hongxeob.motticle.image.application.dto.req;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public record ImageUploadReq(
	List<MultipartFile> file
) {
}
