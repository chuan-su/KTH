package se.sep.finance.domain.model;

import se.sep.common.domain.model.AbstractEntity;
import se.sep.common.domain.model.SepId;
import se.sep.security.domain.model.Department;

public class FinancialRequest extends AbstractEntity  {
  private SepId projectId;

  private Department requestingDepartment;
  private Integer requiredAmount;
  private String reason;

  private Status status;

  public SepId getProjectId() {
    return projectId;
  }

  public void setProjectId(SepId projectId) {
    this.projectId = projectId;
  }

  public Department getRequestingDepartment() {
    return requestingDepartment;
  }

  public void setRequestingDepartment(Department requestingDepartment) {
    this.requestingDepartment = requestingDepartment;
  }

  public Integer getRequiredAmount() {
    return requiredAmount;
  }

  public void setRequiredAmount(Integer requiredAmount) {
    this.requiredAmount = requiredAmount;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
}
