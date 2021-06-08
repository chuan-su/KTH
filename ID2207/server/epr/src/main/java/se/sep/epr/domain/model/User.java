package se.sep.epr.domain.model;

import se.sep.common.domain.model.SepId;
import se.sep.security.domain.model.Department;
import se.sep.security.domain.model.Role;

public class User {
  private SepId userId;
  private String username;
  private Department department;
  private Role role;

  public SepId getUserId() {
    return userId;
  }

  public void setUserId(SepId userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Department getDepartment() {
    return department;
  }

  public void setDepartment(Department department) {
    this.department = department;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }
}
