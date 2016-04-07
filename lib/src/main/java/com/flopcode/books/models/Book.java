package com.flopcode.books.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Book implements Serializable {
  public final String id;
  public final String isbn;
  public final String title;
  public final String authors;
  @SerializedName("user_id")
  public final long userId;
  @SerializedName("location_id")
  public final long locationId;
  @SerializedName("active_checkout_id")
  public long activeCheckout;

  public Book(String id, String isbn, String title, String authors, long user, long location, long activeCheckout) {
    this.id = id;
    this.isbn = isbn;
    this.title = title;
    this.authors = authors;
    this.userId = user;
    this.locationId = location;
    this.activeCheckout = activeCheckout;
  }

  public Book(String isbn, String title, String authors) {
    this(null, isbn, title, authors, -1, -1, -1);
  }

  @Override
  public String toString() {
    return "Book { id = " + id + ", isbn = " + isbn + ", title = " + title + ", authors = " + authors + ", userId = " + userId + ", locationId = " + locationId + " }";
  }

  public boolean isAvailable() {
    return activeCheckout == 0;
  }
}
