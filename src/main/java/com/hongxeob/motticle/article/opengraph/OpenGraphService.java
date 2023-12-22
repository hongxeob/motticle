package com.hongxeob.motticle.article.opengraph;

import java.util.Optional;

public interface OpenGraphService {

	Optional<OpenGraphVO> getMetadata(String url);
}
