import io.vertx.core.AbstractVerticle;

/**
 * Created by schniz and tom boldan on 04/02/2016.
 */
public class Test extends AbstractVerticle {
    public void start2() {
        vertx.createHttpServer().requestHandler(req -> {
            int i = 0;
            i = i + 1;
            i = i - 1;

            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello tom!");
        }).listen(8080);
    }

    public void start() {
    	System.out.println("start");
        vertx.eventBus().consumer("tom", message -> System.out.println(message.body()));
    }
}