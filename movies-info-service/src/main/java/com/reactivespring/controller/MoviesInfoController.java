package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@Slf4j
public class MoviesInfoController {


    private MoviesInfoService moviesInfoService;

    Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().all();

    public MoviesInfoController(MoviesInfoService moviesInfoService){

        this.moviesInfoService = moviesInfoService;

    }
    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value = "year", required = false) Integer year,
                                            @RequestParam(value = "name", required = false) String name){
        log.info("Year is {}:", year);
        if(year != null && name != null)
            return moviesInfoService.getByNameAndYear(year, name);
        else if(year != null)
            return moviesInfoService.getMovieInfoByYear(year);
        else if(name != null)
            return moviesInfoService.getMovieInfoByName(name);
        return moviesInfoService.getAllMovieInfos();
    }
    @GetMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id){
        return moviesInfoService.getMovieInfoById(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping(value = "/movieinfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> getMovieInfoById(){
        return movieInfoSink.asFlux();
    }


    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo){

        return moviesInfoService.addMovieInfo(movieInfo)
                .doOnNext(savedInfo -> movieInfoSink.tryEmitNext(savedInfo));
    }

    @PutMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody @Valid MovieInfo updateMovieInfo, @PathVariable String id){

        return moviesInfoService.updateMovieInfo(updateMovieInfo, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id){

        return moviesInfoService.deleteById(id);
    }
}
