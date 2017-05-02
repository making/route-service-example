package org.cloudfoundry.example;

import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;

import java.util.Optional;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;

import io.netty.channel.Channel;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.NettyContext;
import reactor.ipc.netty.http.server.HttpServer;

public class RouteServiceApplication {

	public static void main(String[] args) throws Exception {
		int port = Optional.ofNullable(System.getenv("PORT")).map(Integer::parseInt)
				.orElse(8080);
		Channel channel = null;
		try {
			HttpServer httpServer = HttpServer.create("0.0.0.0", port);
			Mono<? extends NettyContext> handler = httpServer
					.newHandler(new ReactorHttpHandlerAdapter(httpHandler()));
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("Shut down ...");
			}));
			channel = handler.block().channel();
			channel.closeFuture().sync();
		}
		finally {
			if (channel != null) {
				channel.eventLoop().shutdownGracefully();
			}
		}
	}

	static HttpHandler httpHandler() {
		RouterFunction<?> route = new Controller().routes();
		return toHttpHandler(route);
	}
}
