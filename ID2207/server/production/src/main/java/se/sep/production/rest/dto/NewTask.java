package se.sep.production.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.sep.common.domain.model.SepId;
import se.sep.common.lib.SepIdDeserializer;
import se.sep.production.domain.model.Priority;
import se.sep.security.domain.model.Team;

public class NewTask {
  public String title;
  public String description;
  public Priority priority;

  @JsonDeserialize(using = SepIdDeserializer.class)
  public SepId assignedUserId;

  public Team assignedTeam;
}
