package com.learnkafka.config;

import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
@EnableKafka
public class LibraryConsumerEventConfig {

	@Bean
	ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactor(
			ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			ConsumerFactory<Object, Object> consumerFactory) {
		ConcurrentKafkaListenerContainerFactory<Object, Object> listenerFactory = new ConcurrentKafkaListenerContainerFactory<>();
		configurer.configure(listenerFactory, consumerFactory);
		listenerFactory.setConcurrency(3);
		//listenerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
		return listenerFactory;
	}

}
