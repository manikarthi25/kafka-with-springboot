package com.learnkafka.repo;

import org.springframework.data.repository.CrudRepository;

import com.learnkafka.entity.LibraryEvent;

public interface LibraryEventConsumerRepo extends CrudRepository<LibraryEvent, Integer> {

}
