package com.flopcode.android.books;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.flopcode.android.books.BooksApi.BooksService;
import com.flopcode.android.books.BooksApi.IsbnLookupService;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class Books extends Activity {

  public static final String LOG_TAG = "Books";
  private BooksService booksService;
  private IsbnLookupService isbnLookupService;

  @Bind(R.id.listView)
  ListView books;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    booksService = BooksApi.createBooksService();
    isbnLookupService = BooksApi.createIsbnLookupService();
    setContentView(R.layout.books);
    ButterKnife.bind(this);

    ProgressBar progressBar = new ProgressBar(this);
    progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    progressBar.setIndeterminate(true);
    books.setEmptyView(progressBar);

    books.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Book book = (Book) books.getAdapter().getItem(position);
        startActivity(new Intent(Books.this, ShowBook.class).putExtra("book", book));
      }
    });


    Log.d(LOG_TAG, "onCreate");
    booksService.index().enqueue(new Callback<List<Book>>() {
      @Override
      public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
        List<Book> listOfBooks = response.body();
        final ArrayAdapter<Book> adapter = new ArrayAdapter<>(Books.this, android.R.layout.simple_list_item_1);
        adapter.addAll(listOfBooks);
        books.setAdapter(adapter);
      }

      @Override
      public void onFailure(Call<List<Book>> call, Throwable throwable) {
        ShowBook.toast(Books.this, "could not fetch list of books");
        Log.e(LOG_TAG, "could not fetch list of books", throwable);
      }
    });
    //new GetBookAsyncTask().execute("9780321635365");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.books, menu);
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
      new IntentIntegrator(this).initiateScan();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scanResult != null) {
      Log.d(LOG_TAG, "scanResult.getFormatName(): " + scanResult.getFormatName());
      Log.d(LOG_TAG, "scanResult.getContents(): " + scanResult.getContents());
      new GetBookAsyncTask().execute(scanResult.getContents());
    }
  }

  private class GetBookAsyncTask extends AsyncTask<String, Object, Book> {

    @Override
    protected Book doInBackground(String... params) {
      try {
        return findBookForIsbn(params[0]);
      } catch (Exception e) {
        e.printStackTrace();
        Log.e(LOG_TAG, "could not process " + params[0], e);
      }
      return null;
    }

    @Override
    protected void onPostExecute(Book book) {
      Log.d(LOG_TAG, "adding book: " + book);
      Intent i = new Intent(Books.this, AddBook.class);
      i.putExtra("book", book);
      startActivity(i);
    }
  }

  private Book findBookForIsbn(String isbn) throws Exception {
    return isbnLookupService.find("ISBN:" + isbn).execute().body();
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
