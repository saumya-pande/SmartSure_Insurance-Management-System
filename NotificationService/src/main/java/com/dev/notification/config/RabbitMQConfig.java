package com.dev.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "smartsure.topic.exchange";
    
    // Queues
    public static final String QUEUE_USER_REGISTERED = "email.user.registered.queue";
    public static final String QUEUE_POLICY_PURCHASED = "email.policy.purchased.queue";
    public static final String QUEUE_POLICY_TYPE_CREATED = "log.policy.type.created.queue";

    // Routing Keys
    public static final String ROUTING_KEY_REGISTER = "user.registered";
    public static final String ROUTING_KEY_PURCHASED = "policy.purchased";
    public static final String ROUTING_KEY_CREATED = "policy.type.created";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(QUEUE_USER_REGISTERED, true); // durable
    }

    @Bean
    public Queue policyPurchasedQueue() {
        return new Queue(QUEUE_POLICY_PURCHASED, true);
    }

    @Bean
    public Queue policyTypeCreatedQueue() {
        return new Queue(QUEUE_POLICY_TYPE_CREATED, true);
    }

    @Bean
    public Binding bindingUserRegistered(Queue userRegisteredQueue, TopicExchange exchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(exchange).with(ROUTING_KEY_REGISTER);
    }

    @Bean
    public Binding bindingPolicyPurchased(Queue policyPurchasedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(policyPurchasedQueue).to(exchange).with(ROUTING_KEY_PURCHASED);
    }

    @Bean
    public Binding bindingPolicyTypeCreated(Queue policyTypeCreatedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(policyTypeCreatedQueue).to(exchange).with(ROUTING_KEY_CREATED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
