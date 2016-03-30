package com.flopcode.books.android.test;

import com.flopcode.books.BooksApi;
import com.flopcode.books.models.Location;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationsIntegrationTest extends WithBooksServerTest {
  @Test
  public void testLocationsIndex() throws Exception {
    Call<List<Location>> call = BooksApi.createLocationsService(new URL(booksServer), "key1").index();
    Response<List<Location>> res = call.execute();
    List<Location> locations = res.body();
    assertThat(locations).hasSize(2);
    Location l1 = locations.get(0);
    assertThat(l1.name).isEqualTo("l-one");
    assertThat(l1.description).isEqualTo("desc 1");

    Location l2 = locations.get(1);
    assertThat(l2.name).isEqualTo("l-two");
    assertThat(l2.description).isEqualTo("desc 2");
  }
}
