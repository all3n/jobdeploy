package com.devhc.jobdeploy.web.entity;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Test {

  @Id
  @GeneratedValue
  private Long id;
  private String name;
  private String desc;
  @CreatedDate
  private Date createTime;

  @LastModifiedDate
  private Date updateTime;
}
