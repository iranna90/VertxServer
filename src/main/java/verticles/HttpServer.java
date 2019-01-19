package verticles;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.vertx.core.Future;
import io.vertx.reactivex.RxHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class HttpServer extends AbstractVerticle {

  @Override
  public void start(final Future<Void> startFuture) {

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.post("/").consumes("application/json").handler(this::handle);

    vertx.createHttpServer()
        .requestHandler(router)
        .rxListen(8888)
        .subscribe(
            any -> startFuture.complete(),
            error -> System.out.println("failed deployment " + error.getMessage())
        );
  }

  private void handle(final RoutingContext rc) {
    Observable.just(rc)
        .map(RoutingContext::getBodyAsJson)
        .delay(300, TimeUnit.MILLISECONDS)
        .observeOn(RxHelper.scheduler(vertx.getDelegate()))
        .subscribe(
            body -> rc.response().end(body.encode()),
            error -> {
              error.printStackTrace();
              rc.response().end(error.getMessage());
            }
        );
  }

  public static void main(String[] args) {
    Vertx.vertx().rxDeployVerticle(HttpServer.class.getName())
        .subscribe(
            deploymentId -> System.out.println("Deployed successfully"),
            Throwable::printStackTrace
        );
  }
}
