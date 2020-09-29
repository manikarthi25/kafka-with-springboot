package com.learnkafka.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.learnkafka.helper.LibraryEventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LibraryEvent {
    
	private Integer libraryEventId;
	private LibraryEventType libraryEventType;
	@NotNull(message = " book must not be null")
	@Valid
	private Book book;

}
