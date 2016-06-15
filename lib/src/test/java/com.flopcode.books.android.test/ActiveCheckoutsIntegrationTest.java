package com.flopcode.books.android.test;

import com.flopcode.books.BooksApi;
import com.flopcode.books.BooksApi.ActiveCheckoutsService;
import com.flopcode.books.BooksApi.BooksService;
import com.flopcode.books.models.ActiveCheckout;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ActiveCheckoutsIntegrationTest extends WithBooksServerTest {
  @Test
  public void testCheckoutAndCheckinBook() throws Exception {
    BooksService booksService = BooksApi.createBooksService(booksServer, "key1");
    long id = booksService.index().execute().body().get(0).id;
    final ActiveCheckoutsService activeCheckoutsService = BooksApi.createActiveCheckoutsService(booksServer, "key1");
    ActiveCheckout res = activeCheckoutsService.create(id).execute().body();
    assertThat(res).isNotNull();
    assertThat(activeCheckoutsService.destroy("" + res.id).execute().isSuccess());
  }
}
