package com.hongxeob.motticle.article.opengraph;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.hongxeob.motticle.article.application.dto.res.ArticleOgRes;
import com.hongxeob.motticle.article.application.dto.res.ArticlesOgRes;
import com.hongxeob.motticle.article.application.dto.res.OpenGraphResponse;
import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.article.domain.ArticleType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class OpenGraphProcessor {

	@Qualifier("threadPoolExecutor")
	private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
	private final OpenGraphService openGraphService;

	public ArticlesOgRes generateArticlesOgResWithOpenGraph(Slice<Article> articles) {
		final Map<Long, OpenGraphResponse> inspirationOpenGraphMap = new ConcurrentHashMap<>();
		// executor 에 작업 할당
		final List<CompletableFuture<Void>> completableFutures =
			articles.stream()
				.map(
					article -> CompletableFuture.runAsync(
						() -> inspirationOpenGraphMap.put(
							article.getId(),
							getOpenGraphResponse(article.getType(), article.getContent())
						),
						threadPoolTaskExecutor
					)
				).toList();
		// 비동기 작업 끝날때까지 대기
		completableFutures.forEach(CompletableFuture::join);

		List<ArticleOgRes> articleOgResList = articles.stream()
			.peek(article -> article.setFilePath(article.getContent()))
			.map(article -> ArticleOgRes.of(article, inspirationOpenGraphMap.get(article.getId())))
			.toList();

		return ArticlesOgRes.of(articleOgResList, articles);
	}

	public OpenGraphResponse getOpenGraphResponse(ArticleType articleType, String link) {
		if (articleType != ArticleType.LINK) {
			return OpenGraphResponse.from(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		Optional<OpenGraphVO> openGraphVoOptional = openGraphService.getMetadata(link);

		if (openGraphVoOptional.isEmpty()) {
			return OpenGraphResponse.from(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		OpenGraphVO openGraphVo = openGraphVoOptional.get();

		return OpenGraphResponse.of(
			HttpStatus.OK.value(),
			openGraphVo.getImage(),
			openGraphVo.getTitle(),
			openGraphVo.getUrl() != null ? openGraphVo.getUrl() : link,
			openGraphVo.getDescription()
		);
	}
}
