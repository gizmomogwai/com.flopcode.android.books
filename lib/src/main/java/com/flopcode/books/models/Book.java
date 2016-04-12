package com.flopcode.books.models;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Book implements Serializable {
  public final long id;
  public final String isbn;
  public final String title;
  public final String authors;
  @SerializedName("user_id")
  public final long userId;
  @SerializedName("location_id")
  public final long locationId;
  @SerializedName("active_checkout_id")
  public long activeCheckout;

  public Book(long id, String isbn, String title, String authors, long user, long location, long activeCheckout) {
    this.id = id;
    this.isbn = isbn;
    this.title = title;
    this.authors = authors;
    this.userId = user;
    this.locationId = location;
    this.activeCheckout = activeCheckout;
  }

  public Book(String isbn, String title, String authors) {
    this(0, isbn, title, authors, -1, -1, -1);
  }

  @Override
  public String toString() {
    return "Book { id = " + id + ", isbn = " + isbn + ", title = " + title + ", authors = " + authors + ", userId = " + userId + ", locationId = " + locationId + " }";
  }

  public String status(List<User> users, final long me) {
    if (activeCheckout == 0) {
      return "available";
    }

    if (activeCheckout == me) {
      return "checked out by me";
    }

    User res = Iterables.find(users, new Predicate<User>() {
      @Override
      public boolean apply(User input) {
        return input.id == activeCheckout;
      }
    });
    if (res != null) {
      return "checked out by " + res.name;
    }

    return "checked out by unknown";
  }
}
