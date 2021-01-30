package com.cicadasworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

public class TemplateVerticle extends AbstractVerticle {

    // 1. 声明路由
    Router router;

    // step1 声明模板引擎
    ThymeleafTemplateEngine thymeleafTemplateEngine;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        // 2. 初始化路由
        router = Router.router(vertx);

        // step2 初始化ThymeleafTemplateEngine
        thymeleafTemplateEngine = ThymeleafTemplateEngine.create(vertx);

        // 3. 配置路由解析url
        router.route("/").handler(req -> {
            // step3 模板直接渲染
            JsonObject obj = new JsonObject();
            obj.put("name", "Hello World from backend");
            thymeleafTemplateEngine.render(obj,
                    "templates/index.html",
                    bufferAsyncResult -> {
                        if (bufferAsyncResult.succeeded()) {
                            req.response()
                            .putHeader("content-type", "text/html")
                            .end(bufferAsyncResult.result());
                        } else {

                        }

            });
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x!");
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
