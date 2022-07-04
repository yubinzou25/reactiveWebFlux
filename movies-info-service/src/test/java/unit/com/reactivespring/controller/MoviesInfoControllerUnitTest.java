package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;


@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoService;

    String MOVIES_INFO_URL = "/v1/movieinfos";

    @Test
    void getAllMoviesInfo(){
        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        when(moviesInfoService.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieinfos));

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(2);
    }
    @Test
    void getMovieInfoById(){
        var id = "abc";
        var movieinfos = new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        when(moviesInfoService.getMovieInfoById(isA(String.class))).thenReturn(Mono.just(movieinfos));
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieInfo);
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                });
    }

    @Test
    void addMovieInfo(){
        var movieinfos = new MovieInfo("abcd", "Dark Knight Rises1",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        when(moviesInfoService.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieinfos));
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieinfos)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var addInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(addInfo);
                    assertEquals("abcd", addInfo.getMovieInfoId());
                });
    }
    @Test
    void updateMovieInfo(){
        var movieInfoId = "abc";
        var movieinfos = new MovieInfo(movieInfoId, "Dark Knight Rises1",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        when(moviesInfoService.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(movieinfos));
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(movieinfos)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updateInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(updateInfo);
                    assertNotNull("Dark Knight Rises1", updateInfo.getName());
                });
    }
    @Test
    void deleteMovieInfo(){
        var movieInfoId = "abc";
        when(moviesInfoService.deleteById(isA(String.class))).thenReturn(Mono.empty());
        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
    @Test
    void addMovieInfoValidation(){
        var movieinfos = new MovieInfo("abcd", "",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieinfos)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    System.out.println("responseBody: " + responseBody);
                    var expectedErrorMessage = "movieInfo.name must be present";
                    assertNotNull(responseBody);
                    assertEquals(expectedErrorMessage, responseBody);
                });
    }
}