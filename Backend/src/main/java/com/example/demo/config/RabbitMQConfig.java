package com.example.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    @Value("${app.rabbitmq.exchange:sale.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.dead-letter-exchange:sale.dlx}")
    private String deadLetterExchangeName;

    @Value("${app.rabbitmq.mail-queue:mail.queue}")
    private String mailQueueName;

    @Value("${app.rabbitmq.notification-queue:notification.queue}")
    private String notificationQueueName;

    @Value("${app.rabbitmq.mail-dead-letter-queue:mail.dlq}")
    private String mailDeadLetterQueueName;

    @Value("${app.rabbitmq.notification-dead-letter-queue:notification.dlq}")
    private String notificationDeadLetterQueueName;

    @Value("${app.rabbitmq.mail-routing-key:mail.send}")
    private String mailRoutingKey;

    @Value("${app.rabbitmq.notification-routing-key:notification.create}")
    private String notificationRoutingKey;

    @Value("${app.rabbitmq.mail-dead-letter-routing-key:mail.dead}")
    private String mailDeadLetterRoutingKey;

    @Value("${app.rabbitmq.notification-dead-letter-routing-key:notification.dead}")
    private String notificationDeadLetterRoutingKey;

    @Bean
    public DirectExchange saleExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchangeName, true, false);
    }

    @Bean
    public Queue mailQueue() {
        return QueueBuilder.durable(mailQueueName)
                .deadLetterExchange(deadLetterExchangeName)
                .deadLetterRoutingKey(mailDeadLetterRoutingKey)
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueueName)
                .deadLetterExchange(deadLetterExchangeName)
                .deadLetterRoutingKey(notificationDeadLetterRoutingKey)
                .build();
    }

    @Bean
    public Queue mailDeadLetterQueue() {
        return QueueBuilder.durable(mailDeadLetterQueueName).build();
    }

    @Bean
    public Queue notificationDeadLetterQueue() {
        return QueueBuilder.durable(notificationDeadLetterQueueName).build();
    }

    @Bean
    public Binding mailBinding(@Qualifier("mailQueue") Queue mailQueue, DirectExchange saleExchange) {
        return BindingBuilder.bind(mailQueue).to(saleExchange).with(mailRoutingKey);
    }

    @Bean
    public Binding notificationBinding(@Qualifier("notificationQueue") Queue notificationQueue, DirectExchange saleExchange) {
        return BindingBuilder.bind(notificationQueue).to(saleExchange).with(notificationRoutingKey);
    }

    @Bean
    public Binding mailDeadLetterBinding(@Qualifier("mailDeadLetterQueue") Queue mailDeadLetterQueue,
                                         @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(mailDeadLetterQueue).to(deadLetterExchange).with(mailDeadLetterRoutingKey);
    }

    @Bean
    public Binding notificationDeadLetterBinding(@Qualifier("notificationDeadLetterQueue") Queue notificationDeadLetterQueue,
                                                 @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(notificationDeadLetterQueue).to(deadLetterExchange).with(notificationDeadLetterRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.example.demo.dto.queue");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
