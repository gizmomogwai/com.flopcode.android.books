import com.flopcode.android.books.Book;
import com.flopcode.android.books.BooksApi;
import com.flopcode.android.books.BooksApi.IsbnLookupService;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class Blub {
  @Test
  public void testSomething() throws Exception {
    IsbnLookupService h = BooksApi.createIsbnLookupService();
    Book book = h.find("9781473620919").execute().body();
    System.out.println("book = " + book);
  }
}
