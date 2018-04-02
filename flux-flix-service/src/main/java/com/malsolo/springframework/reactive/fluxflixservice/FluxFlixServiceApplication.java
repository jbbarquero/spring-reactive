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
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class FluxFlixServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FluxFlixServiceApplication.class, args);
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
