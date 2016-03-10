import com.flopcode.android.books.Book;
import com.flopcode.android.books.BooksApi;
import com.flopcode.android.books.BooksApi.IsbnLookupService;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GoogleBooksTest {
  @Test
  public void testSomething() throws Exception {
    IsbnLookupService h = BooksApi.createIsbnLookupService();
    final String isbn = "9781473620919";
    Book book = h.find(isbn).execute().body();
    assertThat(book.title).isEqualTo("Thing Explainer");
    assertThat(book.authors).isEqualTo("Randall Munroe");
    assertThat(book.isbn).isEqualTo(isbn);
  }
}
