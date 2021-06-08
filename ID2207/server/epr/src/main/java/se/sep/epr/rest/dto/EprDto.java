package se.sep.epr.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.sep.common.domain.model.SepId;
import se.sep.common.lib.SepIdSerializer;
import se.sep.epr.domain.model.Preference;
import se.sep.epr.domain.model.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EprDto {
  @JsonSerialize(using = SepIdSerializer.class)
  public SepId sepId;

  public LocalDateTime createdAt;
  public LocalDateTime lastModifiedAt;

  public String clientName;
  public String eventType;
  public String description;

  public int numberOfAttendees;
  public int plannedBudget;

  public LocalDate eventDateFrom;
  public LocalDate eventDateTo;

  public List<Preference> preferences;

  public Status status;
  public UserDto lastModifiedBy;

  public int suggestedDiscount;
  public int suggestedBudget;

  public List<CommentDto> comments;
}
