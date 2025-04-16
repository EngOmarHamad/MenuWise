package com.example.menuwise.network;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MealsClient {

    private static final String BASE_URL = "https://dummyjson.com/";
    private static final int CONNECT_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 30;
    private static final int WRITE_TIMEOUT = 30;

    private static Retrofit retrofit;

    public static MealApiService getApiClient() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(MealApiService.class);
    }
}
