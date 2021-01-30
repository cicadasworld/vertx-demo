package com.cicadasworld;

import java.util.ArrayList;
import java.util.List;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

public class HandleExceptionVerticle extends AbstractVerticle {

    // 1. 声明路由
    Router router;

    // 配置连接参数
    MySQLConnectOptions connectOptions;

    // 配置连接池Pool options
    PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5);

    // Create the client pool
    MySQLPool client;

    // 外部化
    ConfigRetriever retriever;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        retriever = ConfigRetriever.create(vertx);
        this.getConfig(startPromise);
    }

    private Future<JsonObject> getConfig(Promise<Void> startPromise) {
        Promise<JsonObject> promise = Promise.promise();
        retriever.getConfig(ar -> {
            if (ar.succeeded()) {
                JsonObject config = ar.result();
                connectOptions = new MySQLConnectOptions()
                    .setPort(Integer.parseInt(config.getString("port")))
                    .setHost(config.getString("host"))
                    .setDatabase(config.getString("database"))
                    .setUser(config.getString("user"))
                    .setPassword(config.getString("password"));

                client = MySQLPool.pool(vertx, connectOptions, poolOptions);

                // 2. 初始化路由
                router = Router.router(vertx);

                // 3. 配置路由解析url
                router.route("/test/list").handler(req -> {
                    int page;
                    String temp = req.request().getParam("page");
                    if (temp == null) {
                        page = 1;
                    } else {
                        page = Integer.parseInt(temp);
                    }
                    int offset = (page - 1) * 3;

                    // Get a connection from the pool
                    this.getConnection()
                        .compose(conn -> this.getRows(conn, offset))
                        .onSuccess(rows -> {
                            List<JsonObject> list = new ArrayList<>();
                            rows.forEach(item -> {
                                JsonObject json = new JsonObject();
                                json.put("id", item.getValue("id"));
                                json.put("name", item.getValue("userName"));
                                list.add(json);
                            });
                            req.response()
                                .putHeader("content-type", "application/json")
                                .end(list.toString());
                        })
                        .onFailure(throwable -> {
                            req.response()
                                .putHeader("content-type", "application/json")
                                .end(throwable.toString());
                        });
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
        });
        return promise.future();
    }

    // 1. 获取数据库连接
    private Future<SqlConnection> getConnection() {
        Promise<SqlConnection> promise = Promise.promise();
        client.getConnection(ar1 -> {
            if (ar1.succeeded()) {
                System.out.println("Connected");
                SqlConnection conn = ar1.result();

                promise.complete(conn);

            } else {
                promise.fail(ar1.cause());
            }
        });
        return promise.future();
    }

    // 2. 查询数据库
    private Future<RowSet<Row>> getRows(SqlConnection conn, int offset) {
        Promise<RowSet<Row>> promise = Promise.promise();
        conn
            .preparedQuery("select id, userName from smbms_user limit 3 offset ?")
            .execute(Tuple.of(offset), ar2 -> {
                if (ar2.succeeded()) {
                    promise.complete(ar2.result());
                } else {
                    promise.fail(ar2.cause());
                }
                conn.close();
            });

        return promise.future();
    }
}
