package se.maokei.keyvalue_store;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.maokei.keyvalue_store.routes.Api;

import static se.maokei.keyvalue_store.util.CorsHelper.getAllowedHeaders;
import static se.maokei.keyvalue_store.util.CorsHelper.getAllowedMethods;

public class MainVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args){
    VertxOptions vertxOptions = new VertxOptions();

    Vertx.clusteredVertx(vertxOptions, res -> {
      if(res.succeeded()) {

        Vertx vertx  = res.result();
        ConfigRetriever cr = ConfigRetriever.create(vertx);
        cr.getConfig(config -> {
          LOGGER.info("Got config");
          if(config.succeeded()) {
            JsonObject conf = config.result();
            DeploymentOptions options = new DeploymentOptions().setConfig(conf);
            vertx.deployVerticle(new MainVerticle(), options);
            vertx.deployVerticle(new KeyValueStoreVerticle(), options);
          }
        });
      }
    });
  }

  @Override
  public void start(Promise<Void> startPromise) {

    LOGGER.info("MainVerticle start ");
    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create("*")
      .allowedHeaders(getAllowedHeaders())
      .allowedMethods(getAllowedMethods()));

    Api api = new Api(vertx);
    router.mountSubRouter("/", api.getSubRouter());

    int serverPort = 8888;
    if(config().getInteger("http.port") != null) {
      serverPort = config().getInteger("http.port");
    }
    System.out.println("Server port: " + serverPort);

    router.route().handler(StaticHandler.create().setCachingEnabled(false));
    vertx.createHttpServer(new HttpServerOptions().setCompressionSupported(true))
      .requestHandler(router).listen(serverPort, asyncResult -> {
      if(asyncResult.succeeded()) {
        LOGGER.info("Http server is running");
      }else{
        LOGGER.error("Could not start http server", asyncResult.cause());
      }
    });
  }
}
