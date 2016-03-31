package com.flopcode.books.android.test;

import com.flopcode.books.BooksApi;
import com.flopcode.books.models.Book;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BooksIntegrationTest extends WithBooksServerTest {

  @Test
  public void testBooksIndex() throws Exception {
    Call<List<Book>> call = BooksApi.createBooksService(booksServer, "key2").index();
    Response<List<Book>> res = call.execute();
    final List<Book> books = res.body();
    assertThat(books.size()).isEqualTo(2);
  }

  @Test
  public void testBooksShow() throws Exception {
    Call<List<Book>> call1 = BooksApi.createBooksService(booksServer, "key2").index();
    String id = call1.execute().body().get(0).id;

    Call<Book> call = BooksApi.createBooksService(booksServer, "key2").show(id);
    Response<Book> res = call.execute();
    Book book = res.body();
    assertThat(book.id).isEqualTo("1");
    assertThat(book.isbn).isEqualTo("isbn1");
    assertThat(book.title).isEqualTo("title1");
    assertThat(book.authors).isEqualTo("authors1");
    assertThat(book.userId).isEqualTo(1);
    assertThat(book.locationId).isEqualTo(1);
  }


  @Test
  public void testBooksCreate() throws Exception {
    final String isbn = "isbn";
    final String title = "title";
    final String authors = "authors";
    final int userId = 1;
    final int locationId = 1;


    Call<Book> call = BooksApi.createBooksService(booksServer, "key1").create(isbn, title, authors, userId, locationId);
    Response<Book> response = call.execute();
    Book result = response.body();
    assertThat(result.isbn).isEqualTo(isbn);
    assertThat(result.title).isEqualTo(title);
    assertThat(result.authors).isEqualTo(authors);
    assertThat(result.userId).isEqualTo(userId);
    assertThat(result.locationId).isEqualTo(locationId);
  }
}
