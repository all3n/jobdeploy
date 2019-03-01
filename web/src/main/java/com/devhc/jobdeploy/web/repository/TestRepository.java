package com.devhc.jobdeploy.web.repository;

import com.devhc.jobdeploy.web.entity.Test;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource
public interface TestRepository extends PagingAndSortingRepository<Test, Long> {

  @RestResource(path = "name")
  @Query(value = "select t from #{#entityName} t where t.name like %:name%")
  Page<Test> findByNameLike(@Param("name") String name, Pageable p);
}
