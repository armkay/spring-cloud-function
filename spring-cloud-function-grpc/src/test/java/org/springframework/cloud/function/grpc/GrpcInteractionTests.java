/*
 * Copyright 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.function.grpc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author Oleg Zhurakousky
 *
 */
public class GrpcInteractionTests {

	@Test
	public void testRequestReply() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
				SampleConfiguration.class).web(WebApplicationType.NONE).run(
						"--spring.jmx.enabled=false",
						"--spring.cloud.function.definition=uppercase",
						"--spring.cloud.function.grpc.port=" + FunctionGrpcProperties.GRPC_PORT)) {

			Message<byte[]> message = MessageBuilder.withPayload("\"hello gRPC\"".getBytes())
					.setHeader("foo", "bar")
					.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)
					.build();

			Message<byte[]> reply = GrpcUtils.requestReply(message);

			assertThat(reply.getPayload()).isEqualTo("\"HELLO GRPC\"".getBytes());
		}
	}

	@Test
	public void testRequstReplyFunctionDefinitionInMessage() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
				SampleConfiguration.class).web(WebApplicationType.NONE).run(
						"--spring.jmx.enabled=false",
						"--spring.cloud.function.grpc.port=" + FunctionGrpcProperties.GRPC_PORT)) {

			Message<byte[]> message = MessageBuilder.withPayload("\"hello gRPC\"".getBytes())
					.setHeader("foo", "bar")
					.setHeader("spring.cloud.function.definition", "reverse")
					.build();

			Message<byte[]> reply = GrpcUtils.requestReply(message);

			assertThat(reply.getPayload()).isEqualTo("\"CPRg olleh\"".getBytes());
		}
	}

	@Test
	public void testBidirectionalStreamWithImperativeFunction() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
				SampleConfiguration.class).web(WebApplicationType.NONE).run(
						"--spring.jmx.enabled=false",
						"--spring.cloud.function.definition=uppercase",
						"--spring.cloud.function.grpc.port="
								+ FunctionGrpcProperties.GRPC_PORT)) {

			List<Message<byte[]>> messages = new ArrayList<>();
			messages.add(MessageBuilder.withPayload("\"Ricky\"".getBytes()).setHeader("foo", "bar")
					.build());
			messages.add(MessageBuilder.withPayload("\"Julien\"".getBytes()).setHeader("foo", "bar")
					.build());
			messages.add(MessageBuilder.withPayload("\"Bubbles\"".getBytes()).setHeader("foo", "bar")
					.build());

			Flux<Message<byte[]>> clientResponseObserver =
					GrpcUtils.biStreaming("localhost", FunctionGrpcProperties.GRPC_PORT, Flux.fromIterable(messages));

			List<Message<byte[]>> results = clientResponseObserver.collectList().block(Duration.ofSeconds(1));
			assertThat(results.size()).isEqualTo(3);
			assertThat(results.get(0).getPayload()).isEqualTo("\"RICKY\"".getBytes());
			assertThat(results.get(1).getPayload()).isEqualTo("\"JULIEN\"".getBytes());
			assertThat(results.get(2).getPayload()).isEqualTo("\"BUBBLES\"".getBytes());
		}
	}

	@Test
	public void testBidirectionalStreamWithReactiveFunction() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
				SampleConfiguration.class).web(WebApplicationType.NONE).run(
						"--spring.jmx.enabled=false",
						"--spring.cloud.function.definition=uppercaseReactive",
						"--spring.cloud.function.grpc.port="
								+ FunctionGrpcProperties.GRPC_PORT)) {

			List<Message<byte[]>> messages = new ArrayList<>();
			messages.add(MessageBuilder.withPayload("\"Ricky\"".getBytes()).setHeader("foo", "bar")
					.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)
					.build());
			messages.add(MessageBuilder.withPayload("\"Julien\"".getBytes()).setHeader("foo", "bar")
					.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)
					.build());
			messages.add(MessageBuilder.withPayload("\"Bubbles\"".getBytes()).setHeader("foo", "bar")
					.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)
					.build());

			Flux<Message<byte[]>> resultStream =
					GrpcUtils.biStreaming("localhost", FunctionGrpcProperties.GRPC_PORT, Flux.fromIterable(messages));

			List<Message<byte[]>> results = resultStream.collectList().block(Duration.ofSeconds(5));
			assertThat(results.size()).isEqualTo(3);
			assertThat(results.get(0).getPayload()).isEqualTo("\"RICKY\"".getBytes());
			assertThat(results.get(1).getPayload()).isEqualTo("\"JULIEN\"".getBytes());
			assertThat(results.get(2).getPayload()).isEqualTo("\"BUBBLES\"".getBytes());
		}
	}

	@Test
	public void testClientStreaming() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
				SampleConfiguration.class).web(WebApplicationType.NONE).run(
						"--spring.jmx.enabled=false",
						"--spring.cloud.function.definition=streamInStringOut",
						"--spring.cloud.function.grpc.port="
								+ FunctionGrpcProperties.GRPC_PORT)) {

			List<Message<byte[]>> messages = new ArrayList<>();
			messages.add(MessageBuilder.withPayload("\"Ricky\"".getBytes()).setHeader("foo", "bar")
					.build());
			messages.add(MessageBuilder.withPayload("\"Julien\"".getBytes()).setHeader("foo", "bar")
					.build());
			messages.add(MessageBuilder.withPayload("\"Bubbles\"".getBytes()).setHeader("foo", "bar")
					.build());

			Message<byte[]> reply =
					GrpcUtils.clientStream("localhost", FunctionGrpcProperties.GRPC_PORT, Flux.fromIterable(messages));

			assertThat(reply.getPayload()).isEqualTo("[Ricky, Julien, Bubbles]".getBytes());
		}
	}

	@Test
	public void testServerStreaming() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
				SampleConfiguration.class).web(WebApplicationType.NONE).run(
						"--spring.jmx.enabled=false",
						"--spring.cloud.function.definition=stringInStreamOut",
						"--spring.cloud.function.grpc.port="
								+ FunctionGrpcProperties.GRPC_PORT)) {

			Message<byte[]> message = MessageBuilder.withPayload("\"Ricky\"".getBytes()).setHeader("foo", "bar").build();

			Flux<Message<byte[]>> reply =
					GrpcUtils.serverStream("localhost", FunctionGrpcProperties.GRPC_PORT, message);

			List<Message<byte[]>> results = reply.collectList().block(Duration.ofSeconds(5));
			assertThat(results.size()).isEqualTo(2);
			assertThat(results.get(0).getPayload()).isEqualTo("\"Ricky\"".getBytes());
			assertThat(results.get(1).getPayload()).isEqualTo("\"RICKY\"".getBytes());
		}
	}

	@Test
	public void testBiStreamStreamInStringOutFailure() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
				SampleConfiguration.class).web(WebApplicationType.NONE).run(
						"--spring.jmx.enabled=false",
						"--spring.cloud.function.definition=streamInStringOut",
						"--spring.cloud.function.grpc.port="
								+ FunctionGrpcProperties.GRPC_PORT)) {

			List<Message<byte[]>> messages = new ArrayList<>();
			messages.add(MessageBuilder.withPayload("\"Ricky\"".getBytes()).setHeader("foo", "bar")
					.build());
			messages.add(MessageBuilder.withPayload("\"Julien\"".getBytes()).setHeader("foo", "bar")
					.build());
			messages.add(MessageBuilder.withPayload("\"Bubbles\"".getBytes()).setHeader("foo", "bar")
					.build());

			Flux<Message<byte[]>> clientResponseObserver =
					GrpcUtils.biStreaming("localhost", FunctionGrpcProperties.GRPC_PORT, Flux.fromIterable(messages));

			try {
				clientResponseObserver.collectList().block(Duration.ofSeconds(1));
				fail();
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	@Test
	public void testBiStreamStringInStreamOutFailure() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
				SampleConfiguration.class).web(WebApplicationType.NONE).run(
						"--spring.jmx.enabled=false",
						"--spring.cloud.function.definition=stringInStreamOut",
						"--spring.cloud.function.grpc.port="
								+ FunctionGrpcProperties.GRPC_PORT)) {

			List<Message<byte[]>> messages = new ArrayList<>();
			messages.add(MessageBuilder.withPayload("\"Ricky\"".getBytes()).setHeader("foo", "bar")
					.build());
			messages.add(MessageBuilder.withPayload("\"Julien\"".getBytes()).setHeader("foo", "bar")
					.build());
			messages.add(MessageBuilder.withPayload("\"Bubbles\"".getBytes()).setHeader("foo", "bar")
					.build());

			Flux<Message<byte[]>> clientResponseObserver =
					GrpcUtils.biStreaming("localhost", FunctionGrpcProperties.GRPC_PORT, Flux.fromIterable(messages));

			try {
				clientResponseObserver.collectList().block(Duration.ofSeconds(1));
				fail();
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	@EnableAutoConfiguration
	public static class SampleConfiguration {

		@Bean
		public Function<String, String> uppercase() {
			return v -> v.toUpperCase();
		}

		@Bean
		public Function<String, String> reverse() {
			return v -> new StringBuilder(v).reverse().toString();
		}

		@Bean
		public Function<Flux<String>, Flux<String>> uppercaseReactive() {
			return flux -> flux.map(v -> v.toUpperCase());
		}

		@Bean
		public Function<Flux<String>, String> streamInStringOut() {
			return flux -> flux.doOnNext(v -> {
				try {
					Thread.sleep(new Random().nextInt(2000)); // artificial delay
				}
				catch (Exception e) {
					// ignore
				}
			}).collectList().block().toString();
		}

		@Bean
		public Function<String, Flux<String>> stringInStreamOut() {
			return value -> Flux.just(value, value.toUpperCase());
		}
	}
}
