package open.source.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MainVerticle extends AbstractVerticle {

	private static final Integer HTTP_SERVER_LISTENING_PORT = 8888;


	private static Handler<HttpServerRequest> HTTP_SERVER_REQUEST_HANDLER = (httpServerRequest) -> {

		log.info("http server request handler -> (httpServerRequest) {}", httpServerRequest);
		HttpServerResponse httpServerResponse = httpServerRequest.response()
				.putHeader("content-type", "text/plain");
		httpServerResponse.end("Hello from Vert.x!");
	};

	private static void rejectOrResolvePromise(Promise<Void> promiseRequest, AsyncResult<HttpServer> taskResponse) {

		log.info("reject or resolve promise -> (promiseRequest) {} (taskResponse) {}", promiseRequest, taskResponse);
		if (taskResponse.succeeded()) {
			log.info("http server startup -> (port) {} (status) STARTED", HTTP_SERVER_LISTENING_PORT);
			promiseRequest.complete();
		} else {
			log.error("http server startup -> (port) {} (status) FAILED", HTTP_SERVER_LISTENING_PORT);
			promiseRequest.fail(taskResponse.cause());
		}
	}

	@Override
	public void start(Promise<Void> promise) throws Exception {

		log.info("start -> (promise) {}", promise);
		HttpServer httpServer = vertx.createHttpServer();
		log.info("got -> (httpServer) {}", httpServer);
		httpServer = httpServer.requestHandler(HTTP_SERVER_REQUEST_HANDLER);
		log.info("got -> (httpServer) {}", httpServer);
		httpServer = httpServer.listen(HTTP_SERVER_LISTENING_PORT,
				httpServerAsyncResult -> rejectOrResolvePromise(promise, httpServerAsyncResult));
		log.info("got -> (httpServer) {}", httpServer);
	}

}
