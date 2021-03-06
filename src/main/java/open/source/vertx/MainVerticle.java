package open.source.vertx;

import java.util.Map;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import lombok.extern.log4j.Log4j2;
import open.source.vertx.route.Handlers;

@Log4j2
public class MainVerticle extends AbstractVerticle {

	private static final String HTTP_SERVER_HOST = "localhost";
	private static final Integer HTTP_SERVER_LISTENING_PORT = 8888;
	private static final String HTTP_SERVER_CONFIG_PATH = "/conf";
	private static final String CONFIG_FILE_PATH = "conf/config.json";


	@Deprecated
	private static Handler<HttpServerRequest> HTTP_SERVER_REQUEST_HANDLER = (httpServerRequest) -> {

		log.info("http server request handler -> (httpServerRequest) {}", httpServerRequest);

		String requestPath = httpServerRequest.path();
		log.info("identified -> (requestPath) {}", requestPath);

		HttpServerResponse httpServerResponse = httpServerRequest.response();
		Future<Void> voidFuture = null;

		// path based routing can be done from here
		if (requestPath.startsWith("/health")) {
			httpServerResponse = httpServerResponse.putHeader("content-type", "text/plain");
			voidFuture = httpServerResponse.end("UP");
		} else if (requestPath.startsWith("/json")) {
			httpServerResponse = httpServerResponse.putHeader("content-type", "application/json");
			voidFuture = httpServerResponse.end("{\"key\":\"value\", \"arrayKey\":[\"arrayValue1\", \"arrayValue2\"]}");
		} else {
			httpServerResponse = httpServerResponse.putHeader("content-type", "text/plain");
			voidFuture = httpServerResponse.end("Hello from Vert.x!");
		}
	};

	private Router getRouter() {

		Router router = Router.router(vertx);

		// specify the api path based router here

		router.get("/").handler(Handlers.DEFAULT_REQUEST_HANDLER);
		router.get("/health").handler(Handlers.HEALTH_REQUEST_HANDLER);
		router.get("/greeting/:name").handler(Handlers.GREETING_REQUEST_HANDLER);

		return router;
	}

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

	private void loadApplicationPropertiesFromConfigFile() {

		System.out.println("load application properties from config file");
		Map<String, String> existingEnvironmentVariables = System.getenv();
		for (String key : existingEnvironmentVariables.keySet()) {
			String value = existingEnvironmentVariables.get(key);
			System.out.println(key + " : " + value);
		}

		/*
		JsonObject httpJsonObject = new JsonObject()
				.put("host", HTTP_SERVER_HOST)
				.put("port", HTTP_SERVER_LISTENING_PORT)
				.put("path", HTTP_SERVER_CONFIG_PATH);
		ConfigStoreOptions httpStore = new ConfigStoreOptions()
				.setType("http")
				.setConfig(httpJsonObject);
		*/

		JsonObject fileJsonObject = new JsonObject()
				.put("path", CONFIG_FILE_PATH);
		ConfigStoreOptions fileStore = new ConfigStoreOptions()
				.setType("file")
				.setConfig(fileJsonObject);

		ConfigStoreOptions systemPropertyStore = new ConfigStoreOptions()
				.setType("sys");

		ConfigRetrieverOptions options = new ConfigRetrieverOptions()
		//		.addStore(httpStore)
				.addStore(fileStore)
				.addStore(systemPropertyStore);

		// https://vertx.io/docs/vertx-config/java/
		// https://github.com/vert-x3/vertx-config/blob/master/vertx-config/src/main/java/examples/ConfigExamples.java

		/*
		JsonObject allJsonFiles = new JsonObject()
				.put("pattern", "dir/*json");
		JsonObject allPropertyFiles = new JsonObject()
				.put("pattern", "dir/*.properties")
				.put("format", "properties");

		JsonArray jsonArray = new JsonArray().add(allJsonFiles).add(allPropertyFiles);

		JsonObject pathJsonObject = new JsonObject()
				.put("path", "config")
				.put("filesets", jsonArray);
		ConfigStoreOptions dir = new ConfigStoreOptions()
				.setType("directory")
				.setConfig(pathJsonObject);
		*/

		ConfigRetriever configRetriever = ConfigRetriever.create(vertx, options);

		configRetriever.getConfig(jsonObjectAsyncResult -> {
			if (jsonObjectAsyncResult.failed()) {
				System.err.println("failed to load the config file at -> (filePath) " + CONFIG_FILE_PATH);
				Throwable throwable = jsonObjectAsyncResult.cause();
				System.err.println("got -> (throwable) " + throwable);
				System.err.println("got -> (throwableClass) " + throwable.getClass());
				System.err.println("got -> (throwableMessage) " + throwable.getMessage());
				System.err.println("got -> (throwableStackTrace) " + throwable.getStackTrace());
				System.err.println("got -> (throwableCause) " + throwable.getCause());
			} else {
				JsonObject configJsonObject = jsonObjectAsyncResult.result();
				System.out.println("got -> (configJsonObject) " + configJsonObject);
			}
		});

		/*
		Future<JsonObject> configFuture = configRetriever.getConfig();
		JsonObject configJsonObject = configFuture.result();
		System.out.println("got -> (configJsonObject) " + configJsonObject);
		*/
	}

	private void initializeAndConfigureHttpServer(Promise<Void> promise) {

		HttpServer httpServer = vertx.createHttpServer();
		log.info("got -> (httpServer) {}", httpServer);

		// messy approach
		// httpServer = httpServer.requestHandler(HTTP_SERVER_REQUEST_HANDLER);

		httpServer = httpServer.requestHandler(getRouter());
		log.info("got -> (httpServer) {}", httpServer);

		// easy way, where promise was not passed in the start() method as argument
		// Future<HttpServer> httpServerFuture = httpServer.listen(HTTP_SERVER_LISTENING_PORT);

		httpServer = httpServer.listen(HTTP_SERVER_LISTENING_PORT,
				httpServerAsyncResult -> rejectOrResolvePromise(promise, httpServerAsyncResult));
		log.info("got -> (httpServer) {}", httpServer);
	}

	@Override
	public void start(Promise<Void> promise) throws Exception {

		System.out.println("start -> (promise) " + promise);
		loadApplicationPropertiesFromConfigFile();
		initializeAndConfigureHttpServer(promise);
	}

	// TODO : on stop -> decommission

}
