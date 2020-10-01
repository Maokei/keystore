package se.maokei.keyvalue_store;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;

import io.vertx.core.json.JsonObject;
import se.maokei.keyvalue_store.store.KeyStore;
import se.maokei.keyvalue_store.util.Constants;

import java.util.UUID;

import static se.maokei.keyvalue_store.util.Constants.EVENT_LOCAL;

public class KeyValueStoreVerticle extends AbstractVerticle {
  private KeyStore keyStore;
  private DeliveryOptions globalDelivery;
  //Differentiate between global messages
  private String instanceId;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Vertx vertx = getVertx();
    keyStore = KeyStore.getInstance();
    registerConsumer(vertx);
    instanceId = UUID.randomUUID().toString();
    globalDelivery = new DeliveryOptions();
    globalDelivery.addHeader("instanceId", instanceId);
  }

  private void update(JsonObject input) {
    String cmd = input.getString("cmd");
    String data = input.getString("data");
    String key = input.getString("key");
    if(cmd.equals("put")) {
      keyStore.put(key, data);
    }else if(cmd.equals("del")) {
      keyStore.delete(key);
    }
  }

  public void registerConsumer(Vertx vertx) {
    MessageConsumer<String> consumer = vertx.eventBus().consumer(Constants.EVENT_GLOBAL);
    consumer.handler(message -> {
      String id = message.headers().get("instanceId");
      //prevent consumption of the same event that was handled locally
      if (!id.isEmpty() && !id.equals(instanceId)) {
        JsonObject jo = new JsonObject(message.body());
        System.out.println("Got a message from: " + id + "  message: " + jo.toString());
        update(jo);
      }
    });

    vertx.eventBus().consumer(EVENT_LOCAL, message -> {
      System.out.println("Received message: " + message.body());
      JsonObject input = new JsonObject(message.body().toString());

      // Actions PUT and GET
      if(input.getString("cmd").equals("put")) {
        String data = input.getString("data");
        String key = input.getString("key");

        boolean update = keyStore.keyExists(key);
        vertx.eventBus().publish(Constants.EVENT_GLOBAL, input.toString(), globalDelivery);
        keyStore.put(key, data);
        if(update) {
          message.reply("update");
          return;
        }
        message.reply("created");
      }
      else if(input.getString("cmd").equals("get")) {
        String key = input.getString("key");
        String value = (String) keyStore.get(key);
        if(value == null) {
          value = "null";
        }
        message.reply(value);
      }
      else if(input.getString("cmd").equals("del")) {
        String key = input.getString("key");
        keyStore.delete(key);
        message.reply(key);
      }
    });
  }
}
