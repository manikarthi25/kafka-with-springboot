package com.learnkafka.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {

	@NotNull
	private Integer bookId;
	@NotBlank(message = "Book Name must not be blank")
	private String bookName;
	@NotBlank(message = "Book Author must not be blank")
	private String bookAuthor;

}
