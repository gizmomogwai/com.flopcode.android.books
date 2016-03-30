package com.flopcode.books.android.test;

import com.flopcode.books.BooksApi;
import com.flopcode.books.models.User;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class UsersIntegrationTest extends WithBooksServerTest {
  @Test
  public void usersIndexTest() throws Exception {
    Call<List<User>> call = BooksApi.createUsersService(new URL(booksServer), "key1").index();
    Response<List<User>> response = call.execute();
    List<User> users = response.body();
    assertThat(users).hasSize(2);

    User u1 = users.get(0);
    assertThat(u1.name).isEqualTo("name1");
    assertThat(u1.account).isEqualTo("account1");
    assertThat(u1.admin).isTrue();
    User u2 = users.get(1);
    assertThat(u2.name).isEqualTo("name2");
    assertThat(u2.account).isEqualTo("account2");
    assertThat(u2.admin).isFalse();
  }

}
