package se.maokei.keyvalue_store.routes;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class Api {
  private Vertx vertx;

  public Router getRouter(Vertx vert) {
    this.vertx = vertx;
    Router apiRoute = Router.router(vertx);
    //TODO
    return apiRoute;
  }
}
