package se.sep.finance.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.sep.common.domain.model.SepId;
import se.sep.common.lib.SepIdDeserializer;
import se.sep.security.domain.model.Department;

public class NewFinancialRequest {

  @JsonDeserialize(using = SepIdDeserializer.class)
  public SepId projectId;

  public Department requestingDepartment;
  public Integer requiredAmount;
  public String reason;
}
