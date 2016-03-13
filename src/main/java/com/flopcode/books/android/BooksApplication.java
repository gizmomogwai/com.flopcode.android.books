package com.flopcode.books.android;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class BooksApplication {
  public static final String LOG_TAG = "Books";
  public static final String API_KEY = "api-key";

  private static String getPreference(Context c, String s) {
    return getSharedPreferences(c).getString(s, null);
  }

  private static SharedPreferences getSharedPreferences(Context c) {
    return c.getSharedPreferences("books", MODE_PRIVATE);
  }

  public static String getApiKey(Context c) {
    return getPreference(c, API_KEY);
  }

  public static void storeApiKey(Context c, String apiKey) {
    getSharedPreferences(c).edit().putString(API_KEY, apiKey).commit();
  }
}
