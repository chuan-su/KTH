package se.sep.epr.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.sep.common.domain.model.SepId;
import se.sep.common.lib.SepIdSerializer;
import se.sep.security.domain.model.Department;
import se.sep.security.domain.model.Role;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
  @JsonSerialize(using = SepIdSerializer.class)
  public SepId userId;

  public String username;
  public Department department;
  public Role role;
}
