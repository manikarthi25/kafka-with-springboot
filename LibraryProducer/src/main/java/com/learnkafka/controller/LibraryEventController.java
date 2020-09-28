package com.learnkafka.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.dto.LibraryEvent;
import com.learnkafka.helper.LibraryEventType;
import com.learnkafka.producer.LibraryEventProducer;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class LibraryEventController {

	@Autowired
	LibraryEventProducer libraryEventProducer;

	@PostMapping(value = "/postlibraryevent", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent)
			throws JsonProcessingException {

		// libraryEventProducer.sendLibraryEvent(libraryEvent);
		log.info("before message start");
		libraryEvent.setLibraryEventType(LibraryEventType.NEW);
		libraryEventProducer.sendLibraryEvent(libraryEvent);
		// log.info("after message successful. mesaage is : {}" +
		// sendResult.toString());
		return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);

	}

	@PutMapping(value = "/updatelibraryevent", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<?> updateLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent)
			throws JsonProcessingException {

		if (libraryEvent.getLibraryEventId() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please check the request");
		}
		libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
		libraryEventProducer.sendLibraryEvent(libraryEvent);
		return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);

	}

}
