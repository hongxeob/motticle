package com.hongxeob.motticle.image.application.dto.res;

import java.util.List;

public record ImagesRes(
	List<String> imageUrl
) {

	public static ImagesRes from(List<String> imageUrl) {
		return new ImagesRes(imageUrl);
	}
}
