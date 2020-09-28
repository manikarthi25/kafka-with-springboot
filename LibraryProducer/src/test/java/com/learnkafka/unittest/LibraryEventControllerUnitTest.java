package com.learnkafka.unittest;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.controller.LibraryEventController;
import com.learnkafka.dto.Book;
import com.learnkafka.dto.LibraryEvent;
import com.learnkafka.producer.LibraryEventProducer;

@WebMvcTest(LibraryEventController.class)
@AutoConfigureMockMvc
public class LibraryEventControllerUnitTest {

	@Autowired
	MockMvc mockMvc;

	ObjectMapper objectMapper = new ObjectMapper();

	@MockBean
	LibraryEventProducer libraryEventProducer;

	@Test
	public void testPostLibraryEvent() throws Exception {

		Book book = Book.builder().bookId(1).bookName("java").bookAuthor("mani").build();
		LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(book).build();

		String json = objectMapper.writeValueAsString(libraryEvent);

		String url = "/postlibraryevent";

		doNothing().when(libraryEventProducer).sendLibraryEvent(libraryEvent);

		// doNothing().when(libraryEventProducer).sendLibraryEvent(isA(LibraryEvent.class));

		mockMvc.perform(post(url).content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

	@Test
	public void testPostLibraryEvent_4xx() throws Exception {

		//Book book = Book.builder().bookId(1).bookName("java").bookAuthor("mani").build();
		LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(null).build();

		String json = objectMapper.writeValueAsString(libraryEvent);

		String url = "/postlibraryevent";

		doNothing().when(libraryEventProducer).sendLibraryEvent(libraryEvent);

		// doNothing().when(libraryEventProducer).sendLibraryEvent(isA(LibraryEvent.class));

		// String expectedErrorMessage = "book must not be null";
		mockMvc.perform(post(url).content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is4xxClientError());// .andExpect(content().string("book must not be null"));
	}

}
