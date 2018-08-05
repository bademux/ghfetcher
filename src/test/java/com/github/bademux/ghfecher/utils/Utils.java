package com.github.bademux.ghfecher.utils;

import com.github.bademux.ghfecher.client.GitHubClientTest;
import com.github.bademux.ghfecher.model.GitHubRepo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    private static final String PARAM_NAME_PAGE = "page";

    public static Utils.LinkHeaderBuilder getLinkHeaderBuilder(URI uri) {
        return Utils.buildLinkHeader().uri(uri);
    }

    @Builder(builderClassName = "LinkHeaderBuilder", builderMethodName = "buildLinkHeader")
    private static String linkHeaderBuilder(@NonNull URI uri,
                                            int currentPageNumber,
                                            int lastPageNumber) {
        if (currentPageNumber == lastPageNumber) {
            return "Link: ";
        }
        Supplier<UriComponentsBuilder> uriComponentsBuilder = UriComponentsBuilder.fromUri(uri)::cloneBuilder;
        return "Link: " +
                createLink(uriComponentsBuilder, currentPageNumber + 1, "next") +
                ", " +
                createLink(uriComponentsBuilder, lastPageNumber + 1, "last");
    }

    private static String createLink(Supplier<UriComponentsBuilder> uriComponentsBuilder, int pageNumber, String linkName) {
        String nextLink = uriComponentsBuilder.get()
                .replaceQueryParam(PARAM_NAME_PAGE, pageNumber)
                .build()
                .toString();
        return '<' + nextLink + '>' + "; rel=\"" + linkName + "\"";
    }

    public static List<GitHubRepo> createItems(int totalItems) {
        return IntStream.range(0, totalItems)
                .mapToObj(Utils::createItem)
                .collect(toList());
    }

    public static GitHubRepo createItem(int value) {
        GitHubRepo item = new GitHubRepo();
        item.setId(value);
        item.setName("test_name_" + value);
        item.setLanguage("lang_name_" + value);
        item.setCreatedAt(ZonedDateTime.now());
        item.setUpdatedAt(ZonedDateTime.now());
        return item;
    }


}
