package com.github.bademux.ghfecher.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.bademux.ghfecher.model.GitHubRepo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.util.Preconditions;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@RequiredArgsConstructor(staticName = "of")
public class PagingAwareDispatcher extends Dispatcher {

    private final ObjectWriter writer = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .writerFor(new TypeReference<List<GitHubRepo>>() {
            });
    private final List<GitHubRepo> data;

    @SneakyThrows
    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        HttpUrl requestUrl = request.getRequestUrl();
        int perPage = createParam(requestUrl, "per_page");
        int pageNum = createParam(requestUrl, "page");
        int start = calcStart(requestUrl, perPage, pageNum);
        int end = calcEnd(perPage, start);
        List<GitHubRepo> result = data.subList(start, end);
        return new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader(Utils.getLinkHeaderBuilder(requestUrl.uri()).currentPageNumber(pageNum).lastPageNumber(calcTotalPages(perPage)).build())
                .setBody(writer.writeValueAsString(result));
    }

    private int calcStart(HttpUrl requestUrl, int perPage, int pageNum) {
        int start = (pageNum - 1) * perPage;
        Preconditions.checkState(start < data.size(), "illegal params for request %s is given, no data", requestUrl);
        return start;
    }

    private int calcEnd(int perPage, int start) {
        int maxEnd = start + perPage;
        return maxEnd > data.size() ? data.size() : maxEnd;
    }

    private int calcTotalPages(int perPage) {
        return data.size() / perPage;
    }

    private int createParam(HttpUrl requestUrl, String paramName) {
        return Optional.ofNullable(requestUrl.queryParameter(paramName))
                .map(Integer::valueOf)
                .orElseThrow(() -> new IllegalArgumentException(format("For url '%s' got bad queryParam named bad '%s' query param", requestUrl, paramName)));
    }


}
