package se.sep.production.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.sep.common.domain.model.SepId;
import se.sep.common.lib.SepIdSerializer;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectDto {
  @JsonSerialize(using = SepIdSerializer.class)
  public SepId sepId;

  public String name;
  public String description;
  public int budget;

  public List<TaskDto> tasks;

  public LocalDateTime createdAt;
  public LocalDateTime lastModifiedAt;

}
