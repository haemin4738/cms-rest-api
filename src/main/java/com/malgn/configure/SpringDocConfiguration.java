package com.malgn.configure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.QuerydslPredicateOperationCustomizer;

@Configuration
public class SpringDocConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QuerydslPredicateOperationCustomizer queryDslQuerydslPredicateOperationCustomizer() {
        return null;
    }
}