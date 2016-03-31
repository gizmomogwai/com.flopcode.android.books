package com.flopcode.books.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class BooksApplication {
  public static final String LOG_TAG = "Books";
  private static final String API_KEY = "api-key";
  private static final String BOOKS_SERVER = "books-server";

  private static String getPreference(Context c, String s) {
    return getSharedPreferences(c).getString(s, null);
  }

  private static SharedPreferences getSharedPreferences(Context c) {
    return c.getSharedPreferences("books", MODE_PRIVATE);
  }

  public static String getApiKey(Context c) {
    return getPreference(c, API_KEY);
  }

  public static String getBooksServer(Context c) {
    return getPreference(c, BOOKS_SERVER);
  }

  public static void storeApiKey(Context c, String apiKey) {
    getSharedPreferences(c).edit().putString(API_KEY, apiKey).commit();
  }

  public static void toast(Context context, String s) {
    Toast.makeText(context, s, Toast.LENGTH_LONG).show();
  }

}
