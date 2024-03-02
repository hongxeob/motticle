package com.hongxeob.image;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hongxeob.image.dto.FileDto;

public interface ImageService {

	List<FileDto> uploadFiles(List<MultipartFile> files) throws IOException;

	void deleteFile(String requestUrl);

}
