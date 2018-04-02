package com.malsolo.springframework.reactive.fluxflixclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

@SpringBootApplication
public class FluxFlixClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(FluxFlixClientApplication.class, args);
    }

    @Bean
    WebClient webClient() {
        return WebClient.create();
    }

    @Bean
    CommandLineRunner demo(WebClient client) {
        return args -> {
            Movie m = new Movie();
            client.get()
                    .uri("http://localhost:8081/movies")
                    .exchange()
                    .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Movie.class))
                    .filter(movie -> movie.getTitle().toLowerCase().contains("silence"))
                    .subscribe(movie ->
                            client.get()
                                    .uri("http://localhost:8081/movies/{id}/events", movie.getId())
                                    .exchange()
                                    .flatMapMany(clientResponse1 -> clientResponse1.bodyToFlux(MovieEvent.class))
                                    .subscribe(System.out::println)
                    );
        };
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class MovieEvent {
    private Movie movie;
    private LocalDate when;
    private String user;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Movie {
    private String id;
    private String title, genre;
}

