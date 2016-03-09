package com.flopcode.android.books;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Books extends Activity {

  public static final String LOG_TAG = "Books";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.books);
    Log.d(LOG_TAG, "onCreate");
    new GetBookAsyncTask().execute("9780321635365");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
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

  public static class Book implements Serializable {
    public final String isbn;
    public final String authors;
    public final String title;

    public Book(String isbn, String authors, String title) {
      this.isbn = isbn;
      this.authors = authors;
      this.title = title;
    }

    @Override
    public String toString() {
      return "Book { isbn = " + isbn + ", authors = " + authors + ", title = " + title + " }";
    }

    public static Book findForIsbn(String isbn) throws Exception {
      String isbnKey = "ISBN:" + isbn;
      URL u = new URL("https://openlibrary.org/api/books?format=json&bibkeys=" + isbnKey + "&jscmd=data");
      Map b = new Gson().fromJson(new InputStreamReader(u.openStream(), "UTF-8"), Map.class);
      Map book = (Map) b.get(isbnKey);
      String title = (String) book.get("title");
      String authors = Joiner.on(", ").join(Collections2.transform((List) book.get("authors"), new Function<Map, String>() {
        @Override
        public String apply(Map map) {
          return (String) map.get("name");
        }
      }));
      return new Book(isbn, authors, title);
    }

    public void create() {

    }
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
        List<User> users = User.all();
        Log.d(LOG_TAG, "users: " + users);
        return Book.findForIsbn(params[0]);
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

  public static final String BOOKS_SERVER_IP = "192.168.1.16";

  private static class User {

    public long id;
    public String name;
    public String account;

    @Override
    public String toString() {
      return "User { id=" + id + ", name=" + name + ", account=" + account + " }";
    }

    public static List<User> all() throws Exception {
      URL u = new URL("http://" + BOOKS_SERVER_IP + ":3000/users.json");
      JsonParser parser = new JsonParser();
      JsonArray array = parser.parse(new InputStreamReader(u.openStream(), "UTF-8")).getAsJsonArray();
      final Gson gson = new Gson();
      return Lists.transform(Lists.newArrayList(array), new Function<JsonElement, User>() {
        @Override
        public User apply(JsonElement jsonElement) {
          return gson.fromJson(jsonElement, User.class);
        }
      });

    }
  }
}
