package com.flopcode.books.android.views.books;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.Bind;
import com.flopcode.books.BooksApi;
import com.flopcode.books.BooksApi.BooksService;
import com.flopcode.books.BooksApi.LocationsService;
import com.flopcode.books.BooksApi.UsersService;
import com.flopcode.books.android.BooksApplication;
import com.flopcode.books.android.R;
import com.flopcode.books.android.views.PreferencesActivity;
import com.flopcode.books.models.Book;
import com.flopcode.books.models.Location;
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
  private UsersService usersService;

  List<Book> books;

  @Bind(R.id.recycler_view)
  RecyclerView booksList;

  private LocationsService locationsService;
  private List<com.flopcode.books.models.User> users;
  private List<Location> locations;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(LOG_TAG, "Books.onCreate");

    booksService = BooksApi.createBooksService(BooksApplication.getBooksServer(this), BooksApplication.getApiKey(this));
    usersService = BooksApi.createUsersService(BooksApplication.getBooksServer(this), BooksApplication.getApiKey(this));
    locationsService = BooksApi.createLocationsService(BooksApplication.getBooksServer(this), BooksApplication.getApiKey(this));

    setContentView(R.layout.books_index);
    bind(this);

    //booksList.setVisibility(GONE);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(LOG_TAG, "Books.onResume");
    //progressDescription.setText("fetching data from server");

    fetchBooks();
    fetchUsers();
    fetchLocations();
  }

  private void fetchLocations() {
    locationsService.index().enqueue(new Callback<List<Location>>() {
      @Override
      public void onResponse(Call<List<Location>> call, final Response<List<Location>> response) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            //progressDescription.setText("got locations");
            setLocations(response.body());
          }
        });
      }

      @Override
      public void onFailure(Call<List<Location>> call, Throwable t) {
        signalError("could not fetch locations", t);
      }
    });
  }

  private void signalError(final String msg, final Throwable throwable) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        BooksApplication.showError(findViewById(R.id.coordinatorLayout), msg, "settings", new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            startActivity(new Intent(Index.this, PreferencesActivity.class));
          }
        });
        Log.e(LOG_TAG, msg, throwable);
      }
    });

  }

  private void setLocations(List<Location> locations) {
    this.locations = locations;
  }

  private void fetchUsers() {
    usersService.index().enqueue(new Callback<List<com.flopcode.books.models.User>>() {

      @Override
      public void onResponse(Call<List<com.flopcode.books.models.User>> call, final Response<List<com.flopcode.books.models.User>> response) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            //progressDescription.setText("got users");
            setUsers(response.body());
          }
        });
      }

      @Override
      public void onFailure(Call<List<com.flopcode.books.models.User>> call, final Throwable throwable) {
        signalError("could not fetch users", throwable);
      }
    });
  }

  private void fetchBooks() {
    booksService.index().enqueue(new Callback<List<Book>>() {
      @Override
      public void onResponse(Call<List<Book>> call, final Response<List<Book>> response) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            //progressDescription.setText("got books");
            setBooks(response.body());
          }
        });
      }

      @Override
      public void onFailure(Call<List<Book>> call, final Throwable throwable) {
        signalError("could not fetch list of books", throwable);
      }
    });
  }

  private void setUsers(List<com.flopcode.books.models.User> users) {
    this.users = users;
    updateUi();
  }

  private void setBooks(List<Book> books) {
    this.books = books;
    updateUi();
  }

  private void updateUi() {
    if (fetchDataComplete()) {
      switchToRealUI();
    }
  }

  private void switchToRealUI() {
    //progress.setVisibility(GONE);
    //booksList.setVisibility(View.VISIBLE);
  }

  private boolean fetchDataComplete() {
    return books != null;
  }


  /*
  @OnItemClick(R.id.listView)
  public void showBook(AdapterView<?> parent, View view, int position, long id) {
    Book book = (Book) books.getAdapter().getItem(position);
    startActivity(new Intent(Index.this, Show.class).putExtra("book", book));
  }
*/

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
