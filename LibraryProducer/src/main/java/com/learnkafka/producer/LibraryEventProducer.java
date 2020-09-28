package com.learnkafka.producer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.dto.LibraryEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventProducer {

	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	ObjectMapper objectMapper;

	public void sendLibraryEventAsyn(LibraryEvent libraryEvent) throws JsonProcessingException {

		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);

		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);

			}

			@Override
			public void onSuccess(org.springframework.kafka.support.SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}
		});

	}

	protected void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
		log.info("Message sent sucessfully. Key : {} value : {}, result : {}", key, value, result.getRecordMetadata());

	}

	protected void handleFailure(Integer key, String value, Throwable ex) {
		log.error("Error sending the message and exception is : {}", ex.getMessage());
		try {
			throw ex;
		} catch (Throwable throwable) {
			log.error("Error sending the message and exception is : {}", throwable.getMessage());
		}

	}

	public SendResult<Integer, String> sendLibraryEventSync(LibraryEvent libraryEvent) throws JsonProcessingException {

		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);

		// long timeout = 1;
		SendResult<Integer, String> sendResult = null;
		try {
			// sendResult = kafkaTemplate.sendDefault(key, value).get(timeout,
			// TimeUnit.SECONDS);
			sendResult = kafkaTemplate.sendDefault(key, value).get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error sending the message and exception is : {}", e.getMessage());
		}

		return sendResult;
	}

	public ListenableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent)
			throws JsonProcessingException {
		String topic = "library-events";
		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);

		ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, topic, value);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}

			@Override
			public void onSuccess(org.springframework.kafka.support.SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}
		});

		return listenableFuture;

	}

	private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String topic, String value) {
		Integer partition = null;
		List<Header> headers = new ArrayList<>();
		headers.add(new RecordHeader("headerKey", "headerValue".getBytes()));
		return new ProducerRecord<Integer, String>(topic, partition, key, value, headers);
	}
}
