/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.example;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.net.URI;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@RestController
final class Controller {

	static final String FORWARDED_URL = "X-CF-Forwarded-Url";

	static final String PROXY_METADATA = "X-CF-Proxy-Metadata";

	static final String PROXY_SIGNATURE = "X-CF-Proxy-Signature";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final WebClient webClient;

	Controller() {
		this.webClient = WebClient.create();
	}

	public RouterFunction<ServerResponse> routes() {
		return route(incoming(), this::service);
	}

	RequestPredicate incoming() {
		return req -> {
			HttpHeaders h = req.headers().asHttpHeaders();
			return h.containsKey(FORWARDED_URL) && h.containsKey(PROXY_METADATA)
					&& h.containsKey(PROXY_SIGNATURE);
		};
	}

	Mono<ServerResponse> service(ServerRequest req) {
		if (logger.isInfoEnabled()) {
			logger.info("Incoming Request: <{} {},{}>", req.method(), req.uri(),
					req.headers().asHttpHeaders());
		}

		HttpHeaders headers = headers(req.headers().asHttpHeaders());
		URI uri = headers.remove(FORWARDED_URL).stream().findFirst().map(URI::create)
				.orElseThrow(() -> new IllegalStateException(
						String.format("No %s header present", FORWARDED_URL)));

		if (logger.isInfoEnabled()) {
			logger.info("Outgoing Request: <{} {},{}>", req.method(), uri, headers);
		}

		WebClient.RequestHeadersSpec<?> spec = webClient.method(req.method()).uri(uri)
				.headers(headers);
		return req
				.bodyToMono(String.class).<WebClient.RequestHeadersSpec<?>>map(
						((WebClient.RequestBodySpec) spec)::syncBody)
				.switchIfEmpty(Mono.just(spec))
				.flatMap(s -> s.exchange()
						.flatMap(x -> ServerResponse.status(x.statusCode())
								.headers(x.headers().asHttpHeaders()).body(
										x.bodyToMono(String.class), String.class)));
	}

	HttpHeaders headers(HttpHeaders incomingHeaders) {
		HttpHeaders headers = new HttpHeaders();
		headers.putAll(incomingHeaders);
		String host = URI.create(incomingHeaders.getFirst(FORWARDED_URL)).getHost();
		headers.put(HttpHeaders.HOST, Collections.singletonList(host));
		return headers;
	}
}
