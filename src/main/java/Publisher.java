import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

/**
 * Created by schniz and tom boldan on 05/02/2016.
 */
public class Publisher extends AbstractVerticle {
    public void start() {
    	System.out.println("publisher");
        EventBus eb = vertx.eventBus();
       vertx.setPeriodic(1000, (x) ->  eb.send("tom", "hey"));
    }
}
