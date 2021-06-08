package se.sep.epr.rest.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import se.sep.epr.rest.dto.UserDto;
import se.sep.epr.domain.model.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EprUserMapper {

  @InheritInverseConfiguration
  UserDto fromUser(User user);
}
