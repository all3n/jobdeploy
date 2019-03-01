package com.devhc.jobdeploy.web.config;

import java.util.Set;
import javax.persistence.Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@Configuration
@Slf4j
public class AppConfig {

  @Bean
  public RepositoryRestConfigurer repositoryRestConfigurer() {

    return new RepositoryRestConfigurerAdapter() {
      @Override
      public void configureRepositoryRestConfiguration(

          RepositoryRestConfiguration config) {

        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
            false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        final Set<BeanDefinition> beans = provider.findCandidateComponents("com.devhc.jobdeploy.web.entity");
        for (final BeanDefinition bean : beans) {
          try {

            log.info("expose ID {}", bean.getBeanClassName());
            config.exposeIdsFor(Class.forName(bean.getBeanClassName()));
          } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Failed to expose `id` field due to", e);
          }
        }
      }
    };
  }
}
