package se.sep.security.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.sep.common.domain.model.SepId;
import se.sep.common.lib.SepIdSerializer;
import se.sep.security.domain.model.Department;
import se.sep.security.domain.model.Role;
import se.sep.security.domain.model.Team;

public class UserDto {
  @JsonSerialize(using = SepIdSerializer.class)
  public SepId sepId;
  public String authToken;

  public String username;
  public Role role;
  public Department department;
  public Team team;
}
