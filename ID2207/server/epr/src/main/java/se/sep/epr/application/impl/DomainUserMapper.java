package se.sep.epr.application.impl;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import se.sep.epr.domain.model.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DomainUserMapper {

  User toUser(se.sep.security.domain.model.User user);
}
