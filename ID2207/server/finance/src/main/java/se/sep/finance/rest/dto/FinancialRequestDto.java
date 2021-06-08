package se.sep.finance.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.sep.common.domain.model.SepId;
import se.sep.common.lib.SepIdSerializer;
import se.sep.finance.domain.model.Status;
import se.sep.security.domain.model.Department;

import java.time.LocalDateTime;

public class FinancialRequestDto {

  @JsonSerialize(using = SepIdSerializer.class)
  public SepId sepId;

  @JsonSerialize(using = SepIdSerializer.class)
  public SepId projectId;

  public Department requestingDepartment;
  public Integer requiredAmount;
  public String reason;

  public Status status;
  public LocalDateTime createdAt;
  public LocalDateTime lastModifiedAt;

}
