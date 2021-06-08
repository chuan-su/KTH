package se.sep.production.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.sep.common.domain.model.SepId;
import se.sep.common.lib.SepIdSerializer;
import se.sep.production.domain.model.Priority;
import se.sep.security.domain.model.Team;

import java.time.LocalDateTime;

public class TaskDto {
  @JsonSerialize(using = SepIdSerializer.class)
  public SepId sepId;

  public String title;
  public String description;
  public Priority priority;

  @JsonSerialize(using = SepIdSerializer.class)
  public SepId assignedUserId;

  public Team assignedTeam;

  public LocalDateTime createdAt;
  public LocalDateTime lastModifiedAt;
}
