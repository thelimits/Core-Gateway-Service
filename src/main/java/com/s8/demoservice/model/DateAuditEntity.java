package com.s8.demoservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.s8.demoservice.config.deserializer.InstantDeserializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;

@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
public abstract class DateAuditEntity implements Serializable {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    @ApiModelProperty(hidden = true)
    private Instant createdAt;

    @LastModifiedDate
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    @ApiModelProperty(hidden = true)
    private Instant updatedAt;

    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    @ApiModelProperty(hidden = true)
    private Instant activatedAt;
}
