package com.hongxeob.image.dto.res;

import java.util.List;

import com.hongxeob.image.dto.FileDto;

public record ImagesRes(
	String imageUrl
) {

	public static ImagesRes from(List<FileDto> fileDtos) {
		return new ImagesRes(fileDtos.get(0).getUploadFileUrl());
	}

		public static ImagesRes from(String imageUrl) {
		return new ImagesRes(imageUrl);
	}
}
