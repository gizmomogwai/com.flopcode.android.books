package com.flopcode.books.models;

import org.joda.time.DateTime;

// see schema.rb
public class Checkout {
  DateTime from;
  DateTime to;
  String userId;
  String bookId;
}
