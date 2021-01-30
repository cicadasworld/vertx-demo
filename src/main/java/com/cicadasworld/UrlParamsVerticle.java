package com.cicadasworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;

public class UrlParamsVerticle extends AbstractVerticle {

    // 1. 声明路由
    Router router;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        // 2. 初始化路由
        router = Router.router(vertx);

        // 经典模式
        // http://localhost:8888/test?page=1&age=10
        // 以?分隔url与params以&分隔各参数

        // 3. 配置路由解析url
        router.route("/test").handler(req -> {
            String page = req.request().getParam("page");
            String age = req.request().getParam("age");
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("page: " + page + ", age: " + age);
        });

        // REST模式
        // http://localhost:8888/test/1/10
        // 以 / 分隔
        router.route("/test/:page/:age").handler(req -> {
            String page = req.request().getParam("page");
            String age = req.request().getParam("age");
            req.response()
                   .putHeader("content-type", "text/plain")
                   .end("page: " + page + ", age: " + age);
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
