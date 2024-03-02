package com.hongxeob.opengraph;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenGraphVO {
	String description;
	String title;
	String url;
	String image;
}
