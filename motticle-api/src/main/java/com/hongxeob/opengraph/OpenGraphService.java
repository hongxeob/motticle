package com.hongxeob.opengraph;

import java.util.Optional;

public interface OpenGraphService {

	Optional<OpenGraphVO> getMetadata(String url);
}
