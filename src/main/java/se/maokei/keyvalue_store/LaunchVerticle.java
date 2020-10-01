package se.maokei.keyvalue_store;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class LaunchVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(LaunchVerticle.class);

  public JsonObject getConfig() {
    JsonObject json = Vertx.currentContext().config();
    System.out.println("ddd " + json.getString("http.port"));
    if (json.isEmpty()) {
      return new JsonObject(
        vertx.fileSystem()
          .readFileBlocking("configg.json"));
    }
    return json;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    VertxOptions vertxOptions = new VertxOptions();
    JsonObject configFile = config();
    Vertx.clusteredVertx(vertxOptions, res -> {
      if(res.succeeded()) {

        Vertx vertx  = res.result();
        ConfigRetriever cr = ConfigRetriever.create(vertx);

        cr.getConfig(config -> {
          LOGGER.info("Got config");
          if(config.succeeded()) {
            JsonObject conf = config.result();
            if(!configFile.isEmpty()) {
              conf = configFile;
            }
            DeploymentOptions options = new DeploymentOptions().setConfig(conf);
            vertx.deployVerticle(new MainVerticle(), options);
            vertx.deployVerticle(new KeyValueStoreVerticle(), options);
          }
        });
      }
    });
  }
}
