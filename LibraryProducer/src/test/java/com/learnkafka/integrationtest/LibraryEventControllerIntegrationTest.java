package com.learnkafka.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import com.learnkafka.dto.Book;
import com.learnkafka.dto.LibraryEvent;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = { "library-events" }, partitions = 3)
@TestPropertySource(properties = { "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.admin.properties.bootstrap-servers=${spring.embedded.kafka.brokers}" })
public class LibraryEventControllerIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	EmbeddedKafkaBroker embeddedKafkaBroker;

	private Consumer<Integer, String> consumer;

	@BeforeEach
	void setUp() {
		String group = "group1";
		String autoCommit = "true";
		Map<String, Object> configs = new HashMap<>(
				KafkaTestUtils.consumerProps(group, autoCommit, embeddedKafkaBroker));
		consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer())
				.createConsumer();
		embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
	}

	@AfterEach
	void tearDown() {
		consumer.close();
	}

	@Test
	public void testPostLibraryEvent() {
		String url = "/postlibraryevent";
		Book book = Book.builder().bookId(111).bookName("java").bookAuthor("mani").build();
		LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(book).build();

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("content-type", MediaType.APPLICATION_JSON.toString());
		HttpEntity<LibraryEvent> requestEntity = new HttpEntity<>(libraryEvent, httpHeaders);
		ResponseEntity<LibraryEvent> responseLibraryEvent = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
				LibraryEvent.class);

		assertEquals(HttpStatus.CREATED, responseLibraryEvent.getStatusCode());

		ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");
		String expectedRecord = "{\"libraryEventId\":1,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":111,\"bookName\":\"java\",\"bookAuthor\":\"mani\"}}";
		String actualRecord = consumerRecord.value();
		assertEquals(expectedRecord, actualRecord);

	}

}
