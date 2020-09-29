package com.learnkafka.service;

import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.repo.LibraryEventConsumerRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LibraryEventConsumerService {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	LibraryEventConsumerRepo libraryEventConsumerRepo;

	public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord)
			throws JsonMappingException, JsonProcessingException {
		LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
		switch (libraryEvent.getLibraryEventType()) {

		case NEW: {
			save(libraryEvent);
			break;
		}
		case UPDATE: {
			validate(libraryEvent);
			save(libraryEvent);
			break;
		}
		default:
			log.info("Invalid Event Type");
		}
	}

	private void validate(LibraryEvent libraryEvent) {

		if (libraryEvent.getLibraryEventId() == null) {
			throw new IllegalArgumentException("Invalid Library Event Id");
		}

		Optional<LibraryEvent> libraryEventOptional = libraryEventConsumerRepo
				.findById(libraryEvent.getLibraryEventId());
		if (!libraryEventOptional.isPresent()) {
			throw new IllegalArgumentException("This ibrary Event Id is not available");
		}

		log.info("Validation is successful : {}", libraryEventOptional.get());
	}

	private void save(LibraryEvent libraryEvent) {
		libraryEvent.getBook().setLibraryEvent(libraryEvent);
		libraryEventConsumerRepo.save(libraryEvent);
		log.info("Successfully Perissted the library event {} ", libraryEvent);
	}

}
