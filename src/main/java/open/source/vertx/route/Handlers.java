package open.source.vertx.route;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class Handlers {

	public static final Handler<RoutingContext> DEFAULT_REQUEST_HANDLER = (routingContext) -> {
		routingContext.request().response().end("Hello World from Vert.x !");
	};

	public static final Handler<RoutingContext> HEALTH_REQUEST_HANDLER = (routingContext) -> {
		routingContext.request().response().end("UP");
	};

	public static final Handler<RoutingContext> GREETING_REQUEST_HANDLER = (routingContext) -> {
		String name = routingContext.pathParam("name");
		String greeting = String.format("Hello %s !", name);
		routingContext.request().response().end(greeting);
	};

}
