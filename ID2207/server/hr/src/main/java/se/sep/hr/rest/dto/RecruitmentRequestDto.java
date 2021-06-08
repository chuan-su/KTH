package se.sep.hr.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.sep.common.domain.model.SepId;
import se.sep.common.lib.SepIdSerializer;
import se.sep.hr.domain.model.ContractType;
import se.sep.hr.domain.model.Status;
import se.sep.security.domain.model.Department;

import java.time.LocalDateTime;

public class RecruitmentRequestDto {

  @JsonSerialize(using = SepIdSerializer.class)
  public SepId sepId;

  public ContractType contractType;
  public Department requestingDepartment;
  public int yearsOfExperience;
  public String jobTitle;
  public String jobDescription;

  public Status status;
  public LocalDateTime createdAt;
  public LocalDateTime lastModifiedAt;
}
