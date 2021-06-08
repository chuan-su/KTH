package se.sep.epr.rest.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import se.sep.epr.domain.model.Epr;
import se.sep.epr.rest.dto.EprDto;
import se.sep.epr.rest.dto.EprFinanceUpdate;
import se.sep.epr.rest.dto.NewEpr;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EprMapper {

  @InheritInverseConfiguration
  EprDto fromEpr(Epr epr);

  Epr toEpr(NewEpr newEpr);
  Epr toEpr(EprFinanceUpdate eprFinanceUpdate);
}
