package com.cicadasworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class RouterVerticle extends AbstractVerticle {

    // 1. 声明路由
    Router router;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        // 2. 初始化路由
        router = Router.router(vertx);

        // 3. 配置路由解析url
        router.get("/").handler(req -> {
            req.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("greeting", "Hello from Vert.x!").toString());
        });

        // 4. 将路由与vertx HttpServer绑定
        vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port 8888");
            } else {
                startPromise.fail(http.cause());
            }
        });
    }
}
