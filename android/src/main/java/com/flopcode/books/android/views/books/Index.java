package com.flopcode.books.android.views.books;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
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

public class Index extends AppCompatActivity {

  private BooksService booksService;
  private UsersService usersService;

  @Bind(R.id.recycler_view)
  RecyclerView booksList;

  private LocationsService locationsService;
  private List<com.flopcode.books.models.User> users;
  private List<Location> locations;
  private CardAdapter cardAdapter;

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
    booksList.setHasFixedSize(true);
    booksList.setLayoutManager(new LinearLayoutManager(this));
    cardAdapter = new CardAdapter();
    booksList.setAdapter(cardAdapter);
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
        BooksApplication.showError(Index.this, msg, "settings", new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            showPreferences();
          }
        });
        Log.e(LOG_TAG, msg, throwable);
      }
    });

  }

  private void showPreferences() {
    startActivity(new Intent(Index.this, PreferencesActivity.class));
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
    getBooksApplication().setBooks(books);
    updateUi();
  }

  private BooksApplication getBooksApplication() {
    return (BooksApplication) getApplication();
  }

  private void updateUi() {
    if (fetchDataComplete()) {
      switchToRealUI();
    }
  }

  private void switchToRealUI() {
    //progress.setVisibility(GONE);
    //booksList.setVisibility(View.VISIBLE);
    cardAdapter.setData(getBooksApplication().getBooks());
  }

  private boolean fetchDataComplete() {
    return getBooksApplication().getBooks() != null;
  }

  public static void showBook(Context c, Book book) {
    c.startActivity(new Intent(c, Show.class).putExtra("book", book));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.index, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        showPreferences();
        return true;
      case R.id.action_add_book:
        startActivity(new Intent(this, Add.class));
        return true;
      case R.id.action_set_api_key:
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
  
  private static class CardAdapter extends RecyclerView.Adapter<BookHolder> {

    private List<Book> books;

    public void setData(List<Book> books) {
      this.books = books;
      notifyDataSetChanged();
    }

    @Override
    public BookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.books_index_card, parent, false);
      BookHolder viewHolder = new BookHolder(v);
      return viewHolder;
    }

    @Override
    public void onBindViewHolder(BookHolder holder, int i) {
      Book book = books.get(i);
      holder.setBook(book);
    }

    @Override
    public int getItemCount() {
      if (books == null) return 0;

      return books.size();
    }
  }

  static class BookHolder extends RecyclerView.ViewHolder {
    private Book book;
    public TextView title;
    public TextView authors;
    public TextView isbn;

    public BookHolder(View itemView) {
      super(itemView);
      itemView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          showBook(v.getContext(), book);
        }
      });
      title = ButterKnife.findById(itemView, R.id.title);
      authors = ButterKnife.findById(itemView, R.id.authors);
      isbn = ButterKnife.findById(itemView, R.id.isbn);
    }

    public void setBook(Book book) {
      this.book = book;
      title.setText(book.title);
      authors.setText(book.authors);
      isbn.setText(book.isbn);
    }
  }
}
