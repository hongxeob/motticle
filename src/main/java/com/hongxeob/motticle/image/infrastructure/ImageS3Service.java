package com.hongxeob.motticle.image.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.InvalidValueException;
import com.hongxeob.motticle.image.application.ImageService;
import com.hongxeob.motticle.image.application.dto.FileDto;
import com.hongxeob.motticle.image.exception.InvalidFileTypeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageS3Service implements ImageService {
	private final AmazonS3Client amazonS3Client;
	private static final String[] supportedImageExtension = {"jpg", "jpeg", "png", "webp"};

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	public String getUuidFileName(String fileName) {
		String ext = fileName.substring(fileName.indexOf(".") + 1);
		return UUID.randomUUID().toString() + "." + ext;
	}

	@Override
	public List<FileDto> uploadFiles(List<MultipartFile> files) {
		List<FileDto> s3files = new ArrayList<>();
		validateFile(files.get(0));

		String uploadFilePath = UUID.randomUUID() + "/" + getFolderName();

		for (MultipartFile multipartFile : files) {
			String originalFileName = multipartFile.getOriginalFilename();
			String uploadFileName = getUuidFileName(originalFileName);
			String uploadFileUrl = "";

			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentLength(multipartFile.getSize());
			objectMetadata.setContentType(multipartFile.getContentType());

			try (InputStream inputStream = multipartFile.getInputStream()) {
				String keyName = uploadFilePath + "/" + uploadFileName;

				// S3에 폴더 및 파일 업로드
				amazonS3Client.putObject(
					new PutObjectRequest(bucketName, keyName, inputStream, objectMetadata)
						.withCannedAcl(CannedAccessControlList.PublicRead));

				// S3에 업로드한 폴더 및 파일 URL
				uploadFileUrl = "https://kr.object.ncloudstorage.com/" + bucketName + "/" + keyName;
			} catch (IOException e) {
				e.printStackTrace();
			}

			s3files.add(
				FileDto.builder()
					.originalFileName(originalFileName)
					.uploadFileName(uploadFileName)
					.uploadFilePath(uploadFilePath)
					.uploadFileUrl(uploadFileUrl)
					.build());
		}

		return s3files;
	}

	public void deleteFile(String uploadFilePath) {
		String key = extractKeyFromUrl(uploadFilePath);

		amazonS3Client.deleteObject(bucketName, key);
	}

	private String extractKeyFromUrl(String fileUrl) {
		// 파일 URL에서 마지막 슬래시(/) 이후의 부분이 파일 키
		int lastSlashIndex = fileUrl.lastIndexOf("/");
		return fileUrl.substring(lastSlashIndex + 1);
	}

	private String getFolderName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Date date = new Date();
		String str = sdf.format(date);
		return str.replace("-", "/");
	}

	private String getExtension(MultipartFile file) {
		String fileName = file.getOriginalFilename();
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

		return extension;
	}

	private void validateFile(MultipartFile file) {
		log.info("이미지의 유효성 검증을 시작합니다...");
		if (file.isEmpty()) {
			log.warn("EMPTY_IMAGE : {}", file.getOriginalFilename());
			throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
		}

		String inputExtension = getExtension(file);
		log.info("파일의 확장자 : {}", inputExtension);
		boolean isExtensionValid = Arrays.stream(supportedImageExtension)
			.anyMatch(extension -> extension.equalsIgnoreCase(inputExtension));
		log.info("확장자의 유효성 여부 : {}", isExtensionValid);
		if (!isExtensionValid) {
			log.warn("INVALID_IMAGE_FORMAT : {}", file.getOriginalFilename());
			throw new InvalidFileTypeException(ErrorCode.INVALID_IMAGE_FORMAT);
		}
		log.info("이미지의 유효성 검증을 통과했습니다.");
	}
}
