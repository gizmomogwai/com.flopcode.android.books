package com.flopcode.books.models;

public class Location {
  public final long id;
  public final String name;
  public final String description;

  public Location(long id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  @Override
  public String toString() {
    return "Location { id: " + id + ", name: " + name + ", description: " + description + " }";
  }
}
