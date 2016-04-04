package com.flopcode.books.android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View.OnClickListener;
import com.flopcode.books.models.Book;
import com.flopcode.books.models.Location;
import com.flopcode.books.models.User;

import java.util.List;

public class BooksApplication extends Application {
  public static final String LOG_TAG = "Books";
  private static final String API_KEY = "apiKey";
  private static final String BOOKS_SERVER = "booksServer";

  private static String getPreference(Context c, String s) {
    return getSharedPreferences(c).getString(s, null);
  }

  private static String getPreference(Context c, String s, String defaultValue) {
    return PreferenceManager.getDefaultSharedPreferences(c).getString(s, defaultValue);
  }

  private static SharedPreferences getSharedPreferences(Context c) {
    return c.getSharedPreferences("books", MODE_PRIVATE);
  }

  public static String getApiKey(Context c) {
    return getPreference(c, API_KEY);
  }

  public static String getBooksServer(Context c) {
    return getPreference(c, BOOKS_SERVER, "http://192.168.1.100:3000");
  }

  public static void storeApiKey(Context c, String apiKey) {
    getSharedPreferences(c).edit().putString(API_KEY, apiKey).commit();
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

  public List<Book> books;
  public List<User> users;
  public List<Location> locations;

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
}
