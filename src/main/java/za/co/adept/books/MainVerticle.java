/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.adept.books;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 *
 * @author marlon
 */
public class MainVerticle extends AbstractVerticle {

    private InMemoryBookStore store = new InMemoryBookStore();
    
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        
        //GET /books
        getAll(router);
        //GET /books/:isbn -> fetch 1 book
        getBookByISBN(router);
        //POST /books
        createBook(router);
        //PUT /books/:isbn
        updateBook(router);
        //DELETE /books/isbn -> delete one book from in memory store
        deleteBook(router);
        
        registerErrorHandler(router);
        
        vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
            if(http.succeeded()) {
                startFuture.complete();
                System.out.println("HTTP server started on port 8888");
            } else {
                startFuture.fail(http.cause());
            }
        });
    }

    private void deleteBook(Router router) {
        router.delete("/books/:isbn").handler(req -> {
            req.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .end(new JsonObject().put("message", store.deleteBook(req.pathParam("isbn"))).encode());
        });
    }

    private void getBookByISBN(Router router) {
        router.get("/books/:isbn").handler(req -> {
            req.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .end(store.getBook(req.pathParam("isbn")).encode());
        });
    }

    private void registerErrorHandler(Router router) {
        router.errorHandler(500, event -> {
            System.err.println("Failed: " + event.failure().getMessage());
            event.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .end(new JsonObject().put("error", event.failure().getMessage()).encode());
        });
    }

    private void updateBook(Router router) {
        //PUT
        router.put("/books/:isbn").handler(req -> {
            final String isbn = req.pathParam("isbn");
            final JsonObject requestBody = req.getBodyAsJson();
            final Book updatedBook = store.update(isbn, requestBody.mapTo(Book.class));
            // Return response
            req.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .end(requestBody.encode());
        });
    }

    private void createBook(Router router) {
        //POST
        router.post("/books").handler(request -> {
            final JsonObject requestBody = request.getBodyAsJson();
            System.out.println("Request Body: " + requestBody);
            // Store
            store.add(requestBody.mapTo(Book.class));
            // Return response
            request.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .setStatusCode(HttpResponseStatus.CREATED.code())
                    .end(requestBody.encode());
        });
    }

    private void getAll(Router router) {
        //GET
        router.get("/books").handler(req -> {
            req.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .end(store.getAll().encode());
        });
    }
    
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
