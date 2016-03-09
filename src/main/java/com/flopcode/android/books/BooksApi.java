package com.flopcode.android.books;

import com.flopcode.android.books.Books.Book;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class BooksApi {


  private static final String FLUNDER = "192.168.1.100";
  private static final String OFFICE = "192.168.1.100";
  private static final String BLACKBOX = "192.168.1.16";
  public static final String BOOKS_SERVER_IP = BLACKBOX;

  interface BooksService {
    @GET("users.json")
    Call<List<Books.User>> listUsers();

    @GET("books/{id}.json")
    Call<Book> book(@Path("id") String id);

    @GET("books.json")
    Call<List<Book>> index();

    @FormUrlEncoded
    @POST("books.json")
    Call<Book> createBook(@Field("book[isbn]") String isbn, @Field("book[title]") String title, @Field("book[authors]") String authors);


  }

  interface IsbnLookupService {
    @GET("api/books?jscmd=data&format=json")
    Call<Book> find(@Query("bibkeys") String isbn);
  }


  public static BooksService createBooksService() {
    Retrofit rf = retrofitWithLogging()
      .baseUrl("http://" + BOOKS_SERVER_IP + ":3000")
      .addConverterFactory(GsonConverterFactory.create())
      .build();
    return rf.create(BooksService.class);
  }

  private static Retrofit.Builder retrofitWithLogging() {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(Level.BODY);
    OkHttpClient httpClient = new OkHttpClient.Builder()
      .addInterceptor(logging)
      .build();
    return new Retrofit.Builder().client(httpClient);
  }

  public static IsbnLookupService createIsbnLookupService() {
    Retrofit rf = retrofitWithLogging()
      .baseUrl("https://openlibrary.org")
      .addConverterFactory(new Factory() {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
          return new Converter<ResponseBody, Book>() {
            @Override
            public Book convert(ResponseBody responseBody) throws IOException {
              Map b = new Gson().fromJson(responseBody.string(), Map.class);
              String isbn = ((String) b.keySet().iterator().next()).split(":")[1];
              Map book = (Map) b.values().iterator().next();
              String title = (String) book.get("title");
              String authors = Joiner.on(", ").join(Collections2.transform((List) book.get("authors"), new Function<Map, String>() {
                @Override
                public String apply(Map map) {
                  return (String) map.get("name");
                }
              }));
              return new Book(null, isbn, authors, title);
            }
          };
        }
      })
      .build();
    return rf.create(IsbnLookupService.class);
  }

}
