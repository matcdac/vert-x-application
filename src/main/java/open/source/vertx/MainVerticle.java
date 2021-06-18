package open.source.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class MainVerticle extends AbstractVerticle {

	private static final Integer HTTP_SERVER_LISTENING_PORT = 8888;

	private static Handler<HttpServerRequest> HTTP_SERVER_REQUEST_HANDLER = (httpServerRequest) -> {

		HttpServerResponse httpServerResponse = httpServerRequest.response()
				.putHeader("content-type", "text/plain");
		httpServerResponse.end("Hello from Vert.x!");
	};

	private static void rejectOrResolvePromise(Promise<Void> promiseRequest, AsyncResult<HttpServer> taskResponse) {

		if (taskResponse.succeeded()) {
			promiseRequest.complete();
			System.out.println("HTTP server started on port 8888");
		} else {
			promiseRequest.fail(taskResponse.cause());
		}
	}

	@Override
	public void start(Promise<Void> promise) throws Exception {

		HttpServer httpServer = vertx.createHttpServer();
		httpServer = httpServer.requestHandler(HTTP_SERVER_REQUEST_HANDLER);
		httpServer = httpServer.listen(HTTP_SERVER_LISTENING_PORT,
				httpServerAsyncResult -> rejectOrResolvePromise(promise, httpServerAsyncResult));
	}

}
