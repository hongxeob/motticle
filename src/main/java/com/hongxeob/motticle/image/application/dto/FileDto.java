package com.hongxeob.motticle.image.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class FileDto {

	private String originalFileName;
	private String uploadFileName;
	private String uploadFilePath;
	private String uploadFileUrl;

}
