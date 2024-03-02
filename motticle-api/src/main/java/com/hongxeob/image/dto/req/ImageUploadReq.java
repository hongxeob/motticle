package com.hongxeob.image.dto.req;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public record ImageUploadReq(
	List<MultipartFile> file
) {
}
