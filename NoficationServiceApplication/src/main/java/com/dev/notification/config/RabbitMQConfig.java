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
    public static final String QUEUE_CLAIM_STATUS_UPDATED = "email.claim.status.updated.queue";
    public static final String QUEUE_FORGOT_PASSWORD = "email.forgot.password.queue";

    // Routing Keys
    public static final String ROUTING_KEY_REGISTER = "user.registered";
    public static final String ROUTING_KEY_PURCHASED = "policy.purchased";
    public static final String ROUTING_KEY_CREATED = "policy.type.created";
    public static final String ROUTING_KEY_CLAIM_STATUS_UPDATED = "claim.status.updated";
    public static final String ROUTING_KEY_FORGOT_PASSWORD = "user.forgot.password";

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
    public Queue claimStatusUpdatedQueue() {
        return new Queue(QUEUE_CLAIM_STATUS_UPDATED, true);
    }

    @Bean
    public Queue forgotPasswordQueue() {
        return new Queue(QUEUE_FORGOT_PASSWORD, true);
    }

    @Bean
    public Binding bindingUserRegistered(@org.springframework.beans.factory.annotation.Qualifier("userRegisteredQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_REGISTER);
    }

    @Bean
    public Binding bindingPolicyPurchased(@org.springframework.beans.factory.annotation.Qualifier("policyPurchasedQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_PURCHASED);
    }

    @Bean
    public Binding bindingPolicyTypeCreated(@org.springframework.beans.factory.annotation.Qualifier("policyTypeCreatedQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding bindingClaimStatusUpdated(@org.springframework.beans.factory.annotation.Qualifier("claimStatusUpdatedQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_CLAIM_STATUS_UPDATED);
    }

    @Bean
    public Binding bindingForgotPassword(@org.springframework.beans.factory.annotation.Qualifier("forgotPasswordQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_FORGOT_PASSWORD);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
