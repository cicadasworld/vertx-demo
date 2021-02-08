package com.cicadasworld;

import com.cicadasworld.verticle.HelloWorldVerticle;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new HelloWorldVerticle());
    }
}
