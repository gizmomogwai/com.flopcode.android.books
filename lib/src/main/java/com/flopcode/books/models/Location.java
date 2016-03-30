package com.flopcode.books.models;

public class Location {
  public final String name;
  public final String description;

  public Location(String name, String description) {
    this.name = name;
    this.description = description;
  }

  @Override
  public String toString() {
    return "Location { name: " + name + ", description: " + description + " }";
  }
}
