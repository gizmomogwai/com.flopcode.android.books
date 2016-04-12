package com.flopcode.books.android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.flopcode.books.BooksApi;
import com.flopcode.books.BooksApi.BooksService;
import com.flopcode.books.BooksApi.LocationsService;
import com.flopcode.books.BooksApi.UsersService;
import com.flopcode.books.android.views.books.BooksActivity;
import com.flopcode.books.models.Book;
import com.flopcode.books.models.Location;
import com.flopcode.books.models.User;
import com.google.common.collect.Lists;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class BooksApplication extends Application {
  public static final String LOG_TAG = "Books";
  private static final String API_KEY = "apiKey";
  private static final String BOOKS_SERVER = "booksServer";
  private static final String USER_ID = "userId";
  private BooksService booksService;
  private UsersService usersService;
  private LocationsService locationsService;

  public SharedPreferences getSharedPreferences(Context c) {
    return c.getSharedPreferences("books", MODE_PRIVATE);
  }

  private String getPreference(Context c, String s) {
    return getSharedPreferences(c).getString(s, null);
  }

  private long getLongPreference(Context c, String s) {
    return getSharedPreferences(c).getLong(s, 0);
  }

  private String getPreference(Context c, String s, String defaultValue) {
    return PreferenceManager.getDefaultSharedPreferences(c).getString(s, defaultValue);
  }

  public String getBooksServer(Context c) {
    return getPreference(c, BOOKS_SERVER, "http://192.168.1.100:3000");
  }

  public static void showError(Activity c, String s) {
    final Snackbar snackbar = Snackbar.make(c.findViewById(R.id.coordinatorLayout), s, Snackbar.LENGTH_LONG);
    snackbar.show();
  }

  public static void showError(Activity c, String s, String buttonText, OnClickListener onClickListener) {
    final Snackbar snackbar = Snackbar.make(c.findViewById(R.id.coordinatorLayout), s, Snackbar.LENGTH_LONG);
    snackbar.setAction(buttonText, onClickListener);
    snackbar.show();
  }

  public List<Book> books = Lists.newArrayList();
  public List<User> users = Lists.newArrayList();
  public List<Location> locations = Lists.newArrayList();

  public void setBooks(List<Book> books) {
    this.books = books;
  }

  public List<Book> getBooks() {
    return books;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setLocations(List<Location> locations) {
    this.locations = locations;
  }

  public List<Location> getLocations() {
    return locations;
  }

  public void storeApiKey(String apiKey) {
    getSharedPreferences(this).edit().putString(API_KEY, apiKey).commit();
  }

  public String getApiKey(Context c) {
    final String res = getPreference(c, API_KEY);
    Log.e(LOG_TAG, "using api key: " + res);
    return res;
  }

  private void storeUserId(long userId) {
    getSharedPreferences(this).edit().putLong(USER_ID, userId).commit();
  }

  public long getUserId(Context c) {
    return getLongPreference(c, USER_ID);
  }

  public void fetchData(BooksActivity a) {
    final String apiKey = a.getBooksApplication().getApiKey(this);
    booksService = BooksApi.createBooksService(a.getBooksApplication().getBooksServer(this), apiKey);
    usersService = BooksApi.createUsersService(a.getBooksApplication().getBooksServer(this), apiKey);
    locationsService = BooksApi.createLocationsService(a.getBooksApplication().getBooksServer(this), apiKey);
    fetchBooks(a);
    fetchUsers(a);
    fetchLocations(a);
  }

  public void fetchBooks(final BooksActivity a) {
    booksService.index().enqueue(new Callback<List<Book>>() {
      @Override
      public void onResponse(Call<List<Book>> call, final Response<List<Book>> response) {
        a.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            //progressDescription.setText("got books");
            setBooks(response.body());
            a.dataChanged();
          }
        });
      }

      @Override
      public void onFailure(Call<List<Book>> call, final Throwable throwable) {
        signalError(a, "could not fetch list of books", throwable);
      }
    });
  }

  public void fetchLocations(final BooksActivity a) {
    locationsService.index().enqueue(new Callback<List<Location>>() {
      @Override
      public void onResponse(Call<List<Location>> call, final Response<List<Location>> response) {
        a.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            //progressDescription.setText("got locations");
            setLocations(response.body());
            a.dataChanged();
          }
        });
      }

      @Override
      public void onFailure(Call<List<Location>> call, Throwable t) {
        signalError(a, "could not fetch locations", t);
      }
    });
  }

  private void signalError(final BooksActivity a, final String msg, final Throwable throwable) {
    a.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        BooksApplication.showError(a, msg, "settings", new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            a.showPreferences();
          }
        });
        Log.e(LOG_TAG, msg, throwable);
      }
    });

  }

  public void fetchUsers(final BooksActivity a) {
    usersService.index().enqueue(new Callback<List<com.flopcode.books.models.User>>() {

      @Override
      public void onResponse(Call<List<com.flopcode.books.models.User>> call, final Response<List<com.flopcode.books.models.User>> response) {
        a.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            //progressDescription.setText("got users");
            setUsers(response.body());
            a.dataChanged();
          }
        });
      }

      @Override
      public void onFailure(Call<List<com.flopcode.books.models.User>> call, final Throwable throwable) {
        signalError(a, "could not fetch users", throwable);
      }
    });
  }

  public void storeUserIdAndApiKeyFromUri(Uri uri) {
    final List<String> pathSegments = uri.getPathSegments();
    long userId = Long.parseLong(pathSegments.get(1));
    String apiKey = pathSegments.get(3);
    storeApiKey(apiKey);
    storeUserId(userId);
  }


}
