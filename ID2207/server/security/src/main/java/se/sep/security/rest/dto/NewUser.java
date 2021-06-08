package se.sep.security.rest.dto;

import se.sep.security.domain.model.Department;
import se.sep.security.domain.model.Role;
import se.sep.security.domain.model.Team;

public class NewUser {
  public String username;
  public String password;
  public Role role;
  public Department department;
  public Team team;
}
