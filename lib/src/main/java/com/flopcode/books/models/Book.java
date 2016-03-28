package com.flopcode.books.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Book implements Serializable {
  public final String id;
  public final String isbn;
  public final String title;
  public final String authors;
  @SerializedName("user_id")
  public final String userId;
  @SerializedName("location_id")
  public final String locationId;

  public Book(String id, String isbn, String title, String authors, String user, String location) {
    this.id = id;
    this.isbn = isbn;
    this.title = title;
    this.authors = authors;
    this.userId = user;
    this.locationId = location;
  }

  @Override
  public String toString() {
    return "Book { id = " + id + ", isbn = " + isbn + ", title = " + title + ", authors = " + authors + ", userId = " + userId + ", locationId = " + locationId + " }";
  }
}
