import com.flopcode.books.android.BooksApi;
import com.flopcode.books.android.BooksApi.IsbnLookupService;
import com.flopcode.books.android.models.Book;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GoogleBooksTest {
  @Test
  public void testIsbnLookup() throws Exception {
    IsbnLookupService h = BooksApi.createIsbnLookupService();
    final String isbn = "9781473620919";
    Book book = h.find(isbn).execute().body();
    assertThat(book.title).isEqualTo("Thing Explainer");
    assertThat(book.authors).isEqualTo("Randall Munroe");
    assertThat(book.isbn).isEqualTo(isbn);
  }

}

