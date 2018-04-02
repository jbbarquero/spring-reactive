package com.malsolo.springframework.reactive.fluxflixservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class FluxFlixServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FluxFlixServiceApplication.class, args);
	}

	@Bean
	RouterFunction<?> routes(FluxFlixService fluxFlixService) {
	    return RouterFunctions
            .route(RequestPredicates.GET("/movies"),
                request -> ok().body(fluxFlixService.all(), Movie.class)
            )
            .andRoute(RequestPredicates.GET("/movies/{id}"),
                request -> ok().body(fluxFlixService.byId(request.pathVariable("id")), Movie.class)
            )
            .andRoute(RequestPredicates.GET("/movies/{id}/events"),
                request -> ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(fluxFlixService.byId(request.pathVariable("id"))
                        .flatMapMany(fluxFlixService::streamStreams), MovieEvent.class)
            )
            ;
    }

	@Bean
	CommandLineRunner demo(MovieRepository movieRepository) {
		return args -> Stream.of("Aeon Flux", "Enter the Mono<Void>", "The Fluximator",
				"Silence of the Lambdas", "Reactive Monos on Plane", "Y tu Mono tambien",
				"Attack of the fluxes", "Back to the future")
				.map(name -> new Movie(UUID.randomUUID().toString(), name, randomGenre()))
				.forEach(movie -> movieRepository.save(movie).subscribe(System.out::println));
	}

	private String randomGenre() {
		String[] genres = {"horror", "romance", "comedy", "drama", "documentary"};
		return genres[new Random().nextInt(genres.length)];
	}
}

@RestController
@RequestMapping("/moviez")
class MovieRestController {

	private final FluxFlixService fluxFlixService;

	MovieRestController(FluxFlixService fluxFlixService) {
		this.fluxFlixService = fluxFlixService;
	}

	@GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<MovieEvent> events(@PathVariable String id) {
		return this.fluxFlixService.byId(id).flatMapMany(this.fluxFlixService::streamStreams);
	}

	@GetMapping
	public Flux<Movie> all() {
		return this.fluxFlixService.all();
	}

	@GetMapping(value = "/{id}")
	public Mono<Movie> byId(@PathVariable String id) {
		return this.fluxFlixService.byId(id);
	}
}

@Service
class FluxFlixService {
	private final MovieRepository movieRepository;

	FluxFlixService(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	public Flux<MovieEvent> streamStreams(Movie movie) {
	    Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

	    Flux<MovieEvent> events = Flux.fromStream(Stream.generate(() -> new MovieEvent(movie, LocalDate.now(), randomUser())));

	    return Flux.zip(interval, events).map(Tuple2::getT2);
    }

	public Flux<Movie> all() {
		return this.movieRepository.findAll();
	}

	public Mono<Movie> byId(String id) {
		return this.movieRepository.findById(id);
	}

	private String randomUser() {
		String[] users = "user1,user2,user3,user5".split(",");
		return users[new Random().nextInt(users.length)];
	}
}

@Data
class MovieEvent {
    private final Movie movie;
    private final LocalDate when;
    private final String user;
}

interface MovieRepository extends ReactiveMongoRepository<Movie, String> {
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Movie {
	@Id
	private String id;
	private String title, genre;
}
