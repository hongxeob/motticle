package com.hongxeob.opengraph;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hongxeob.article.dto.res.ArticleOgRes;
import com.hongxeob.article.dto.res.ArticlesOgRes;
import com.hongxeob.article.dto.res.OpenGraphResponse;
import com.hongxeob.domain.article.Article;
import com.hongxeob.domain.article.ArticleType;

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
		final Map<Long, OpenGraphResponse> articleOgMap = new ConcurrentHashMap<>();
		final List<CompletableFuture<Void>> completableFutures =
			articles.stream()
				.map(
					article -> CompletableFuture.runAsync(
						() -> articleOgMap.put(
							article.getId(),
							getOpenGraphResponse(article.getType(), article.getContent())
						),
						threadPoolTaskExecutor
					)
				).toList();

		completableFutures.forEach(CompletableFuture::join);

		List<ArticleOgRes> articleOgResList = articles.stream()
			.map(article -> ArticleOgRes.of(article, articleOgMap.get(article.getId())))
			.toList();

		return ArticlesOgRes.of(articleOgResList, articles);
	}

	public OpenGraphResponse getOpenGraphResponse(ArticleType articleType, String link) {
		if (articleType != ArticleType.LINK) {
			return OpenGraphResponse.from(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		OpenGraphVO openGraphVO = openGraphService.getMetadata(link)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ARTICLE_LINK => {}", link);
				return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
			});

		return OpenGraphResponse.of(HttpStatus.OK.value(), openGraphVO);
	}
}
