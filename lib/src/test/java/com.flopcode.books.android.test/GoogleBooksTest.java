package com.flopcode.books.android.test;

import com.flopcode.books.BooksApi;
import com.flopcode.books.BooksApi.IsbnLookupService;
import com.flopcode.books.models.Book;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GoogleBooksTest {
  @Test
  public void testIsbnLookup() throws Exception {
    IsbnLookupService h = BooksApi.createIsbnLookupService();
    final String isbn = "9780201633610";
    Book book = h.find(isbn).execute().body();
    assertThat(book.title).isEqualTo("Design Patterns");
    assertThat(book.authors).isEqualTo("Ralph Johnson, Erich Gamma, John Vlissides, Richard Helm");
    assertThat(book.isbn).isEqualTo(isbn);
  }

}

