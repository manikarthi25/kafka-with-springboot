package com.learnkafka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.dto.LibraryEvent;
import com.learnkafka.producer.LibraryEventProducer;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class LibraryEventController {

	@Autowired
	LibraryEventProducer libraryEventProducer;

	@PostMapping(value = "/v1/postevent", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<LibraryEvent> postEvent(@RequestBody LibraryEvent libraryEvent)
			throws JsonProcessingException {

		// libraryEventProducer.sendLibraryEvent(libraryEvent);
		log.info("before message start");
		SendResult<Integer, String> sendResult = libraryEventProducer.sendLibraryEventSync(libraryEvent);
		log.info("after message successful. mesaage is : {}" + sendResult.toString());
		return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);

	}

}
