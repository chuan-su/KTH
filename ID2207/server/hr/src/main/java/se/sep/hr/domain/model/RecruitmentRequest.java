package se.sep.hr.domain.model;

import se.sep.common.domain.model.AbstractEntity;
import se.sep.security.domain.model.Department;

public class RecruitmentRequest extends AbstractEntity {
  private ContractType contractType;
  private Department requestingDepartment;
  private int yearsOfExperience;
  private String jobTitle;
  private String jobDescription;
  private Status status;

  public ContractType getContractType() {
    return contractType;
  }

  public void setContractType(ContractType contractType) {
    this.contractType = contractType;
  }

  public Department getRequestingDepartment() {
    return requestingDepartment;
  }

  public void setRequestingDepartment(Department requestingDepartment) {
    this.requestingDepartment = requestingDepartment;
  }

  public int getYearsOfExperience() {
    return yearsOfExperience;
  }

  public void setYearsOfExperience(int yearsOfExperience) {
    this.yearsOfExperience = yearsOfExperience;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getJobDescription() {
    return jobDescription;
  }

  public void setJobDescription(String jobDescription) {
    this.jobDescription = jobDescription;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
}
