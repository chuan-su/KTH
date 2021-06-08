package se.sep.hr.rest.dto;

import se.sep.hr.domain.model.ContractType;
import se.sep.security.domain.model.Department;

public class NewRecruitmentRequest {
  public ContractType contractType;
  public Department requestingDepartment;
  public int yearsOfExperience;
  public String jobTitle;
  public String jobDescription;
}
