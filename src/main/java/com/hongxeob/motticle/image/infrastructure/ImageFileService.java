package com.hongxeob.motticle.image.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.image.application.ImageService;
import com.hongxeob.motticle.image.exception.ImageIOException;

@Service
public class ImageFileService implements ImageService {
	private final String uploadDir = "/Users/hongxeob/Desktop/image";

	@Override
	public List<String> add(List<MultipartFile> files) {
		try {
			// 저장할 디렉토리 경로
			Path uploadPath = Path.of(uploadDir);

			// 디렉토리가 없다면 생성
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			return files.stream()
				.map(file -> {
					String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
					String fileExtension = StringUtils.getFilenameExtension(fileName);
					String newFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "." + fileExtension;

					// 저장할 파일 경로
					Path filePath = uploadPath.resolve(newFileName);

					// 파일 복사
					try {
						Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						throw new ImageIOException(ErrorCode.IMAGE_UPLOAD_FAILED);
					}

					return newFileName;
				})
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new ImageIOException(ErrorCode.IMAGE_UPLOAD_FAILED);
		}
	}

	@Override
	public void delete(String fileName) {
		Path filePath = Path.of(uploadDir).resolve(fileName);
		try {
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			throw new ImageIOException(ErrorCode.IMAGE_DELETE_FAILED);
		}
	}

	@Override
	public String getFilePath(String content) {
		Path filePath = Path.of(uploadDir).resolve(content);

		return filePath.toString();
	}
}