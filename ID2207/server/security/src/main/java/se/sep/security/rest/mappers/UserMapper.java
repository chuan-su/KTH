package se.sep.security.rest.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import se.sep.security.domain.model.User;
import se.sep.security.rest.dto.NewUser;
import se.sep.security.rest.dto.UserDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  @InheritInverseConfiguration
  UserDto fromUser(User user);

  User toUser(NewUser newUser);
}
