package se.maokei.keyvalue_store.util;

import io.vertx.core.http.HttpMethod;

import java.util.HashSet;

public class CorsHelper {
  public HashSet<String> getAllowedHeaders() {
    HashSet<String> set = new HashSet<>();
    set.add("x-requested-with");
    set.add("Access-Control-Allow-Origin");
    set.add("Access-Control-Allow-Headers");
    set.add("Access-Control-Request-Method");
    set.add("Authorization");
    set.add("X-PINGARUNER");
    set.add("origin");
    set.add("Content-Type");
    set.add("accept");
    return set;
  }

  public HashSet<HttpMethod> getAllowedMethods() {
    HashSet<HttpMethod> set = new HashSet<>();
    set.add(HttpMethod.POST);
    set.add(HttpMethod.GET);
    set.add(HttpMethod.DELETE);
    set.add(HttpMethod.PATCH);
    set.add(HttpMethod.OPTIONS);
    set.add(HttpMethod.PUT);
    return set;
  }
}
