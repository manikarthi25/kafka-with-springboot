package com.learnkafka.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.consumer.LibraryEventConsumer;
import com.learnkafka.entity.Book;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.entity.LibraryEventType;
import com.learnkafka.repo.LibraryEventConsumerRepo;
import com.learnkafka.service.LibraryEventConsumerService;

@SpringBootTest
@EmbeddedKafka(topics = { "library-events" }, partitions = 3)
@TestPropertySource(properties = { "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.consumer.bootstrap-servers=${spr" + "ing.embedded.kafka.brokers}" })
public class LibraryEventConsumerIntegrationTest {

	@Autowired
	EmbeddedKafkaBroker embeddedKafkaBroker;

	@Autowired
	LibraryEventConsumerRepo libraryEventConsumerRepo;

	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	KafkaListenerEndpointRegistry endpointRegistry;

	@SpyBean
	LibraryEventConsumer libraryEventConsumerSpy;

	@SpyBean
	LibraryEventConsumerService libraryEventConsumerServiceSpy;

	@BeforeEach
	void setUp() {
		for (MessageListenerContainer messageListenerContainer : endpointRegistry.getAllListenerContainers()) {
			ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
		}
	}

	@AfterEach
	void tearDown() {
		libraryEventConsumerRepo.deleteAll();
	}

	@Test
	public void publishNewLibraryEvent()
			throws InterruptedException, JsonMappingException, JsonProcessingException, ExecutionException {
		// given
		String json = "{\"libraryEventId\":111,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":111,\"bookName\":\"java\",\"bookAuthor\":\"mani\"}}";
		kafkaTemplate.sendDefault(json).get();

		// when
		CountDownLatch latch = new CountDownLatch(1);
		long timeout = 3;
		latch.await(timeout, TimeUnit.SECONDS);

		// then
		/*
		 * int wantedNumberOfInvocations = 1; verify(libraryEventConsumerSpy,
		 * times(wantedNumberOfInvocations)) .onMessage((ConsumerRecord<Integer,
		 * String>) isA(ConsumerRecord.class)); verify(libraryEventConsumerServiceSpy,
		 * times(wantedNumberOfInvocations))
		 * .processLibraryEvent((ConsumerRecord<Integer, String>)
		 * isA(ConsumerRecord.class));
		 */

		List<LibraryEvent> libraryEventList = (List<LibraryEvent>) libraryEventConsumerRepo.findAll();
		libraryEventList.forEach(libraryEvent -> {
			assert libraryEvent.getLibraryEventId() == 111;
			String expectedBookName = "Java";
			assertEquals(expectedBookName, libraryEvent.getBook().getBookId());
		});

	}

	@Test
	public void publishUpdateLibraryEvent()
			throws JsonMappingException, JsonProcessingException, InterruptedException, ExecutionException {

		// given
		String json = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":11,\"bookName\":\"java\",\"bookAuthor\":\"mani\"}}";
		LibraryEvent libraryEvent = objectMapper.readValue(json, LibraryEvent.class);
		libraryEvent.getBook().setLibraryEvent(libraryEvent);
		libraryEventConsumerRepo.save(libraryEvent);

		// publish the update library event
		Book updatedBook = Book.builder().bookId(11).bookName("Dot Net").bookAuthor("mani").build();
		libraryEvent.setBook(updatedBook);
		libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
		String updateJson = objectMapper.writeValueAsString(libraryEvent);
		kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), updateJson).get();

		// when
		CountDownLatch latch = new CountDownLatch(1);
		long timeout = 3;
		latch.await(timeout, TimeUnit.SECONDS);

		// then
		LibraryEvent actualResult = libraryEventConsumerRepo.findById(libraryEvent.getLibraryEventId()).get();
		String expectedBookName = "Dot Net";
		//assertEquals(expectedBookName, actualResult.getBook().getBookName());

	}

}
