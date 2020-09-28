package com.learnkafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
public class LibraryEventConsumerManualOffset implements AcknowledgingMessageListener<Integer, String> {

	@Override
	@KafkaListener(topics = { "library-events" })
	public void onMessage(ConsumerRecord<Integer, String> consumerRecord, Acknowledgment acknowledgment) {
		log.info("Consumer Record : {}", consumerRecord);
		acknowledgment.acknowledge();
	}

}
