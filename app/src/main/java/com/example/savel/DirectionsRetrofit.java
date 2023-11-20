package com.example.savel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DirectionsRetrofit {
    // @GET( EndPoint-자원위치(URI) )
    @GET("maps/api/directions/json") //HTTP 메서드 및 URL
    //Requests 타입의 DTO 데이터와 API 키를 요청

    Call<DirectionsResponse> getPosts(@Query("origin") String origin, @Query("destination") String destination,
                                      @Query("mode") String mode, @Query("waypoints") String waypoints,
                                      @Query("key") String key);
}
