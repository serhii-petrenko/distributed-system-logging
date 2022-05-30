package com.course.bff.books.services;

import com.course.bff.books.controlles.BookController;
import com.course.bff.books.models.Book;
import com.course.bff.books.requests.CreateBookCommand;
import com.course.bff.books.responses.AuthorResponse;
import com.course.bff.books.responses.BookResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.asynchttpclient.*;
import org.asynchttpclient.util.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
public class BookService {
    private final static Logger logger = LoggerFactory.getLogger(BookService.class);

    private final ArrayList<Book> books;
    @Value("${authorService}")
    private String authorService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.topic}")
    private String redisTopic;

    public BookService() {
        books = new ArrayList<>();
    }

    public Collection<Book> getBooks() {
//        int i = 0;
//        while (i < 5) {
//            try {
//                Thread.sleep(1_000);
//                i++;
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        throw new IllegalArgumentException("Request params was missed");
        return this.books;
    }

    public Optional<Book> findById(UUID id) {
        return this.books.stream().filter(book -> !book.getId().equals(id)).findFirst();
    }

    @NewSpan(name = "REDIS")
    public void sendPushNotification(BookResponse bookResponse) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            redisTemplate.convertAndSend(redisTopic, gson.toJson(bookResponse));
        } catch (Exception e) {
            logger.error("Push Notification Error", e);
        }
    }

    public Book create(CreateBookCommand createBookCommand) {
        Optional<AuthorResponse> authorSearch = getAutor(createBookCommand.getAuthorId());
        if (authorSearch.isEmpty()) {
            throw new RuntimeException("Author isn't found");
        }

        Book book = new Book(UUID.randomUUID())
                .withTitle(createBookCommand.getTitle())
                .withAuthorId(authorSearch.get().getId())
                .withPages(createBookCommand.getPages());

        this.books.add(book);
        return book;
    }

    private Optional<AuthorResponse> getAutor(UUID authorId) {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config().setConnectTimeout(500);
        AsyncHttpClient client = Dsl.asyncHttpClient(clientBuilder);
        Request socketRequest = new RequestBuilder(HttpConstants.Methods.GET)
                .setUrl(authorService + "/api/v1/authors/" + authorId.toString())
                .build();

        ListenableFuture<Response> socketFuture = client.executeRequest(socketRequest);
        try {
            Response response = socketFuture.get();
            if (response.getStatusCode() != HttpStatus.OK.value()) {
                return Optional.empty();
            }

            AuthorResponse authorResponse = new Gson()
                    .fromJson(response.getResponseBody(), AuthorResponse.class);

            return Optional.of(authorResponse);

        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
    }
}
