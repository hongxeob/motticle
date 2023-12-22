package com.hongxeob.motticle.article.opengraph;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.github.siyoon210.ogparser4j.OgParser;
import com.github.siyoon210.ogparser4j.OpenGraph;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenGraphServiceImpl implements OpenGraphService {
	private final OgMetaElementHtmlParser ogMetaElementHtmlParser;

	@Override
	public Optional<OpenGraphVO> getMetadata(String url) {
		OgParser ogParser = new OgParser(ogMetaElementHtmlParser);
		OpenGraph openGraph = ogParser.getOpenGraphOf(url);
		if (openGraph.getAllProperties().isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(
			OpenGraphVO.builder()
				.image(getValueSafely(openGraph, "image"))
				.title(getValueSafely(openGraph, "title"))
				.url(getValueSafely(openGraph, "url"))
				.description(getValueSafely(openGraph, "description"))
				.build()
		);
	}

	private String getValueSafely(OpenGraph openGraph, String property) {
		final OpenGraph.Content content;
		try {
			content = openGraph.getContentOf(property);
		} catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
			return null;
		}
		return content.getValue();
	}
}
