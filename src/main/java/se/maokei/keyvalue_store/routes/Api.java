package se.maokei.keyvalue_store.routes;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import static se.maokei.keyvalue_store.util.Constants.EVENT_LOCAL;

/**
 * Quite a bit of duplicated code, should use more enums
 * */
public class Api {
  private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);
  private final Vertx vertx;
  private final EventBus eventBus;
  public static String BASE_ROUTE = "/kvstore/values";
  private final DeliveryOptions localDelivery;

  public Api(Vertx vertx) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    localDelivery = new DeliveryOptions();
    localDelivery.setLocalOnly(true);
  }

  public void defaultRoute(RoutingContext rc) {
    JsonObject jo = new JsonObject();
    jo.put("message", "FROM api route");
    rc.response().setStatusCode(200).putHeader("content-header", "application/json")
      .end(Json.encodePrettily(jo));
  }

  public void healthCheck(RoutingContext rc) {
    JsonObject jo = new JsonObject();
    jo.put("status", "available");
    rc.response().setStatusCode(200).putHeader("content-header", "application/json")
      .end(Json.encodePrettily(jo));
  }

  /**
  * putValue
  *
  * @param rc Vertx routing context
  * */
  public void putValue(RoutingContext rc) {
    HttpServerRequest req = rc.request();
    req.pause();
    JsonObject body = rc.getBodyAsJson();
    String key = req.getParam("key");
    String data = body.getString("data");
    rc.request().resume();
    try {
      if (key == null || data == null) {
        LOGGER.error("PUT, no key or no data in body");
        rc.response().setStatusCode(400).end("No key");
        return;
      }
      //json payload
      JsonObject jo = new JsonObject();
      jo.put("cmd", "put");
      jo.put("key", key);
      jo.put("data", data);
      //send and handle message response
      vertx.eventBus().request(EVENT_LOCAL, jo, localDelivery, reply -> {
        if(reply.succeeded()) {
          String message = reply.result().body().toString();
          int code = 201;
          if(message.equals("update"))
            code = 204;
          rc.response().setStatusCode(code).putHeader("content-header", "application/json")
            .end(message);
        }
        else {
          rc.response().setStatusCode(500).putHeader("content-header", "application/json")
            .end("Put, Internal message error");
        }
      });
    }
    catch(Exception e) {
      e.printStackTrace();
      rc.response().setStatusCode(500).putHeader("content-header", "application/json")
        .end("PUT, Internal server error");
    }
  }

  /**
   * getValue
   *
   * @param rc Vertx routing context
   * */
  public void getValue(RoutingContext rc) {
    final EventBus eventBus = vertx.eventBus();
    try {
      String key = rc.request().getParam("key");
      if(key == null) {
        LOGGER.error("GET, no key");
        rc.response().setStatusCode(400).end("No key");
        return;
      }

      JsonObject jo = new JsonObject();
      jo.put("cmd", "get");
      jo.put("key", key);
      eventBus.request(EVENT_LOCAL, jo, localDelivery, reply -> {
        if(reply.succeeded()) {
          String data = reply.result().body().toString();
          rc.response().setStatusCode(200).putHeader("content-header", "application/json")
            .end(data);
        }
        else {
          rc.response().setStatusCode(500).putHeader("content-header", "application/json")
            .end("GET, Internal server error");
        }
      });
    }
    catch(Exception e) {
      rc.response().setStatusCode(500).putHeader("content-header", "application/json")
        .end("GET, Internal server error");
    }
  }

  /**
   * deleteValue
   *
   * @param rc Vertx routing context
   * */
  public void deleteValue(RoutingContext rc) {
    try {
      String key = rc.request().getParam("key");
      if(key == null) {
        LOGGER.error("DELETE, no key");
        rc.response().setStatusCode(400).end("No key");
        return;
      }

      JsonObject jo = new JsonObject();
      jo.put("cmd", "del");
      jo.put("key", key);
      eventBus.request(EVENT_LOCAL, jo, localDelivery, reply -> {
        if(reply.succeeded()) {
          String data = reply.result().body().toString();
          rc.response().setStatusCode(200).putHeader("content-header", "application/json")
            .end("Deleted key: " + key);
        }
        else {
          rc.response().setStatusCode(500).putHeader("content-header", "application/json")
            .end("DELETE, Internal server error");
        }
      });
    }
    catch(Exception e) {
      rc.response().setStatusCode(500).putHeader("content-header", "application/json")
        .end("DELETE, Internal server error");
    }
  }

  public Router getSubRouter() {
    Router apiRoute = Router.router(vertx);
    apiRoute.route("/").handler(this::defaultRoute);
    apiRoute.route("/*").handler(BodyHandler.create());
    apiRoute.get("/healthcheck").handler(this::healthCheck);
    apiRoute.put(BASE_ROUTE + "/:key").handler(this::putValue);
    apiRoute.get(BASE_ROUTE + "/:key").handler(this::getValue);
    apiRoute.delete(BASE_ROUTE + "/:key").handler(this::deleteValue);
    return apiRoute;
  }
}
