package com.reactivespring.controller;

import com.reactivespring.client.MovieInfoRestClient;
import com.reactivespring.client.ReviewRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
public class MoviesController {

    private MovieInfoRestClient movieInfoRestClient;
    private ReviewRestClient reviewRestClient;

    public MoviesController(MovieInfoRestClient movieInfoRestClient, ReviewRestClient reviewRestClient) {
        this.movieInfoRestClient = movieInfoRestClient;
        this.reviewRestClient = reviewRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String movieId){

        return movieInfoRestClient.retrieveMovieInfoById(movieId)
                .flatMap(movieInfo -> {
                    var reviewsListMono = reviewRestClient.retrieveReviews(movieId)
                            .collectList();
                    return reviewsListMono.map(reviews -> new Movie(movieInfo, reviews));
                });
    }
    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> retrieveMovieInfoStream(){

        return movieInfoRestClient.retrieveMovieInfoStream();
    }
}
