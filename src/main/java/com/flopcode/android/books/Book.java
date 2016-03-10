package com.flopcode.android.books;

import java.io.Serializable;

public class Book implements Serializable {
  public final String id;
  public final String isbn;
  public final String title;
  public final String authors;

  public Book(String id, String isbn, String title, String authors) {
    this.id = id;
    this.isbn = isbn;
    this.title = title;
    this.authors = authors;
  }

  @Override
  public String toString() {
    return "Book { id = " + id + ", isbn = " + isbn + ", title = " + title + ", authors = " + authors + " }";
  }
}
