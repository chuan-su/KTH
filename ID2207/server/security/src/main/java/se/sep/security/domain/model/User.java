package se.sep.security.domain.model;

import se.sep.common.domain.model.AbstractEntity;

public class User extends AbstractEntity {

  private String username;
  private String password;
  private String authToken;
  private Role role;
  private Department department;
  private Team team;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToke) {
    this.authToken = authToke;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Department getDepartment() {
    return department;
  }

  public void setDepartment(Department department) {
    this.department = department;
  }

  public Team getTeam() {
    return team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }
}
