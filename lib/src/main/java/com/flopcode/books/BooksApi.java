package com.flopcode.books;

import com.flopcode.books.models.ActiveCheckout;
import com.flopcode.books.models.Book;
import com.flopcode.books.models.Location;
import com.flopcode.books.models.User;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BooksApi {

  private static final String FLUNDER_HOME = "192.168.1.100";
  private static final String OFFICE = "172.31.2.34";
  private static final String BLACKBOX = "192.168.1.16";
  public static final String BOOKS_SERVER_IP = FLUNDER_HOME;
  private final static String API = "api/v1";

  private final static String BOOKS_API = API + "/books";
  private final static String LOCATIONS_API = API + "/locations";
  private final static String ACTIVE_CHECKOUTS_API = API + "/active_checkouts";
  private final static String USERS_API = API + "/users";


  public interface UsersService {
    @GET(USERS_API)
    @Headers("Accept: application/json")
    Call<List<User>> index();
  }

  public interface LocationsService {
    @GET(LOCATIONS_API)
    @Headers("Accept: application/json")
    Call<List<Location>> index();
  }

  public interface ServerAliveService {
    @HEAD(BOOKS_API)
    Call<Void> alive();
  }

  public interface BooksService {

    @GET(BOOKS_API)
    @Headers("Accept: application/json")
    Call<List<Book>> index();

    @FormUrlEncoded
    @POST(BOOKS_API)
    @Headers("Accept: application/json")
    Call<Book> create(@Field("book[isbn]") String isbn,
                      @Field("book[title]") String title,
                      @Field("book[authors]") String authors,
                      @Field("book[user_id]") long userId,
                      @Field("book[location_id]") long locationId);

    @GET(BOOKS_API + "/{id}")
    @Headers("Accept: application/json")
    Call<Book> show(@Path("id") String id);

  }

  public static BooksService createBooksService(String url, String apiKey) {
    Retrofit rf = retrofitWithLogging(apiKey)
      .baseUrl(url)
      .addConverterFactory(GsonConverterFactory.create())
      .build();
    return rf.create(BooksService.class);
  }

  public static UsersService createUsersService(String url, String apiKey) {
    Retrofit rf = retrofitWithLogging(apiKey)
      .baseUrl(url)
      .addConverterFactory(GsonConverterFactory.create())
      .build();
    return rf.create(UsersService.class);

  }

  public static LocationsService createLocationsService(String url, String apiKey) {
    Retrofit rf = retrofitWithLogging(apiKey)
      .baseUrl(url)
      .addConverterFactory(GsonConverterFactory.create())
      .build();
    return rf.create(LocationsService.class);
  }

  public static ServerAliveService createServerAliveService(String url, String apiKey) {
    Retrofit rf = retrofitWithLogging(apiKey)
      .baseUrl(url)
      .build();
    return rf.create(ServerAliveService.class);
  }

  public interface ActiveCheckoutsService {
    @FormUrlEncoded
    @POST(ACTIVE_CHECKOUTS_API)
    @Headers("Accept: application/json")
    Call<ActiveCheckout> create(@Field("book") long bookId);

    @DELETE(ACTIVE_CHECKOUTS_API + "/{id}")
    @Headers("Accept: application/json")
    Call<Void> destroy(@Path("id") String activeCheckoutId);
  }

  public static ActiveCheckoutsService createActiveCheckoutsService(String url, String apiKey) {
    Retrofit rf = retrofitWithLogging(apiKey)
      .baseUrl(url)
      .addConverterFactory(GsonConverterFactory.create())
      .build();
    return rf.create(ActiveCheckoutsService.class);
  }

  private static Retrofit.Builder retrofitWithLogging(String apiKey) {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(Level.BODY);
    OkHttpClient httpClient = new OkHttpClient.Builder()
      .addInterceptor(logging)
      .addNetworkInterceptor(new AddAuthorizationHeaderInterceptor(apiKey))
      .connectTimeout(2, TimeUnit.SECONDS)
      .readTimeout(2, TimeUnit.SECONDS)
      .writeTimeout(2, TimeUnit.SECONDS)
      .build();
    return new Retrofit.Builder().client(httpClient);
  }

  public interface IsbnLookupService {
    @GET("books/v1/volumes")
    Call<Book> find(@Query("q") String isbn);
  }

  public static IsbnLookupService createIsbnLookupService() {
    Retrofit rf = retrofitWithLogging(null)
      .baseUrl("https://www.googleapis.com")
      .addConverterFactory(googleJsonFactory())
      .build();

    return rf.create(IsbnLookupService.class);
  }

  private static Factory googleJsonFactory() {
    return new Factory() {
      @Override
      public Converter<ResponseBody, Book> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return googleJsonConverter();
      }
    };
  }

  private static Converter<ResponseBody, Book> googleJsonConverter() {
    return new Converter<ResponseBody, Book>() {
      @Override
      public Book convert(ResponseBody responseBody) throws IOException {
        Map m = new Gson().fromJson(new StringReader(responseBody.string()), Map.class);
        Map firstItem = (Map) ((List) m.get("items")).iterator().next();
        Map volumeInfo = (Map) firstItem.get("volumeInfo");
        String title = (String) volumeInfo.get("title");
        String authors = Joiner.on(", ").join((List) volumeInfo.get("authors"));
        Map isbnMap = (Map) Iterables.find((List) volumeInfo.get("industryIdentifiers"),
          new Predicate() {
            @Override
            public boolean apply(Object o) {
              Map m = (Map) o;
              return m.get("type").equals("ISBN_13");
            }
          });
        String isbn = (String) isbnMap.get("identifier");
        return new Book(isbn, title, authors);
      }
    };
  }

  private static class AddAuthorizationHeaderInterceptor implements Interceptor {
    private final String apiKey;

    public AddAuthorizationHeaderInterceptor(String apiKey) {
      this.apiKey = apiKey;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
      if (apiKey != null) {
        Request originalRequest = chain.request();
        String token = "Token token=" + apiKey;
        Request newRequest = originalRequest.newBuilder()
          .header("Authorization", token)
          .build();
        return chain.proceed(newRequest);
      } else {
        return chain.proceed(chain.request());
      }
    }
  }
}
        /*new Factory() {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
          return new Converter<ResponseBody, Book>() {
            @Override
            public Book convert(ResponseBody responseBody) throws IOException {
              System.out.println("responseBody.string() = " + responseBody.string());
              Map b = new Gson().fromJson(responseBody.string(), Map.class);
              System.out.println("b.get(\"items\").class = " + b.get("items").getClass());
              return null;
            }
          };
        }
      }*/
  /*
  interface IsbnLookupService {
    @GET("api/books?jscmd=data&format=json")
    Call<Book> find(@Query("bibkeys") String isbn);
  }
*/
/*
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
*/
