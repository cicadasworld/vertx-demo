package com.cicadasworld.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class BodyVerticle extends AbstractVerticle {

    // 1. 声明路由
    Router router;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        // 2. 初始化路由
        router = Router.router(vertx);

        // 获取body参数
        router.route().handler(BodyHandler.create());

        // 3. 配置路由解析url

        // form-data 格式
        // 请求头中的 content-type: application/x-www-form-urlencoded
        router.route("/test/form").handler(req -> {
            String page = req.request().getFormAttribute("page");
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("page: " + page);

        });

        // json格式数据
        // 请求头中的 content-type: application/json
        router.route("/test/json").handler(req -> {
            JsonObject page = req.getBodyAsJson();
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end(page.toString());
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
