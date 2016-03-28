package com.flopcode.books.android.views.books;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import butterknife.Bind;
import butterknife.OnItemClick;
import com.flopcode.books.BooksApi;
import com.flopcode.books.BooksApi.BooksService;
import com.flopcode.books.android.BooksApplication;
import com.flopcode.books.android.R;
import com.flopcode.books.models.Book;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

import static butterknife.ButterKnife.bind;
import static com.flopcode.books.android.BooksApplication.LOG_TAG;

public class Index extends Activity {

  private BooksService booksService;

  @Bind(R.id.listView)
  ListView books;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "Books.onCreate");

    booksService = BooksApi.createBooksService("http://localhost:3000", BooksApplication.getApiKey(this));
    setContentView(R.layout.books_index);
    bind(this);

    ProgressBar progressBar = new ProgressBar(this);
    progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    progressBar.setIndeterminate(true);
    this.books.setEmptyView(progressBar);
  }

  @OnItemClick(R.id.listView)
  public void showBook(AdapterView<?> parent, View view, int position, long id) {
    Book book = (Book) books.getAdapter().getItem(position);
    startActivity(new Intent(Index.this, Show.class).putExtra("book", book));
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(LOG_TAG, "Books.onResume");
    booksService.index().enqueue(new Callback<List<Book>>() {
      @Override
      public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
        List<Book> listOfBooks = response.body();
        final ArrayAdapter<Book> adapter = new ArrayAdapter<>(Index.this, android.R.layout.simple_list_item_1);
        adapter.addAll(listOfBooks);
        books.setAdapter(adapter);
      }

      @Override
      public void onFailure(Call<List<Book>> call, Throwable throwable) {
        BooksApplication.toast(Index.this, "could not fetch list of books");
        Log.e(LOG_TAG, "could not fetch list of books", throwable);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.index, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    if (id == R.id.action_add_book) {
      startActivity(new Intent(this, Add.class));
      return true;
    }

    if (id == R.id.action_set_api_key) {
      new IntentIntegrator(this).initiateScan();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    Log.d(LOG_TAG, "Index.onActivityResult " + requestCode + ", " + resultCode + ", " + intent);
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scanResult != null) {
      Uri uri = Uri.parse(scanResult.getContents());
      if (uri.getScheme().equals("books-api-key")) {
        String apiKey = uri.getLastPathSegment();
        BooksApplication.storeApiKey(this, apiKey);
      }
    }
  }

  public static class User {
    public long id;
    public String name;
    public String account;

    @Override
    public String toString() {
      return "User { id=" + id + ", name=" + name + ", account=" + account + " }";
    }

  }
}
