package com.flopcode.books.models;

import com.google.gson.annotations.SerializedName;

public class ActiveCheckout {
  public final String id;
  @SerializedName("book_id")
  public final String bookId;
  @SerializedName("checkout_id")
  public final String checkoutId;

  public ActiveCheckout(String id, String bookId, String checkoutId) {
    this.id = id;
    this.bookId = bookId;
    this.checkoutId = checkoutId;
  }

  @Override
  public String toString() {
    return "ActiveCheckout { id: " + id + ", bookId: " + bookId + ", checkoutId: " + checkoutId + " }";
  }
}
