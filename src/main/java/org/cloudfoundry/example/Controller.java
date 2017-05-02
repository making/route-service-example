/*
 * Copyright 2016 the original author or authors.
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

import java.net.URI;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

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

	@RequestMapping(headers = { FORWARDED_URL, PROXY_METADATA, PROXY_SIGNATURE })
	Mono<ResponseEntity<String>> service(RequestEntity<String> incoming) {
        this.logger.info("Incoming Request: {}", incoming);

        RequestEntity<?> outgoing = getOutgoingRequest(incoming);
        this.logger.info("Outgoing Request: {}", outgoing);

		WebClient.RequestHeadersSpec<?> spec = this.webClient.method(outgoing.getMethod())
				.uri(outgoing.getUrl()).headers(outgoing.getHeaders());
		if (outgoing.getBody() != null) {
			spec = ((WebClient.RequestBodySpec) spec).syncBody(outgoing.getBody());
		}
		return spec.exchange()
				.flatMap(x -> x.bodyToMono(String.class)
						.map(s -> ResponseEntity.status(x.statusCode())
								.headers(x.headers().asHttpHeaders()).body(s)));
	}

    private static RequestEntity<?> getOutgoingRequest(RequestEntity<?> incoming) {
		HttpHeaders headers = new HttpHeaders();
		HttpHeaders incomingHeaders = incoming.getHeaders();
		headers.putAll(incomingHeaders);
		String host = URI.create(incomingHeaders.getFirst(FORWARDED_URL)).getHost();
		headers.put(HttpHeaders.HOST, Collections.singletonList(host));

        URI uri = headers.remove(FORWARDED_URL).stream()
            .findFirst()
            .map(URI::create)
            .orElseThrow(() -> new IllegalStateException(String.format("No %s header present", FORWARDED_URL)));

        return new RequestEntity<>(incoming.getBody(), headers, incoming.getMethod(), uri);
    }

}
