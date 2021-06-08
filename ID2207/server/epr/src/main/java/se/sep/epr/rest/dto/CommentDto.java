package se.sep.epr.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentDto {
  public String comment;
  public LocalDateTime createdAt;
  public UserDto commentedBy;
}
