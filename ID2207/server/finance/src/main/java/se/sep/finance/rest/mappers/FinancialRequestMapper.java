package se.sep.finance.rest.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import se.sep.finance.domain.model.FinancialRequest;
import se.sep.finance.rest.dto.FinancialRequestDto;
import se.sep.finance.rest.dto.NewFinancialRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FinancialRequestMapper {
  FinancialRequestMapper INSTANCE = Mappers.getMapper(FinancialRequestMapper.class);

  @InheritInverseConfiguration
  FinancialRequestDto fromFinancialRequest(FinancialRequest financialRequest);

  FinancialRequest toFinancialRequest(NewFinancialRequest newFinancialRequest);

  FinancialRequest toFinancialRequest(FinancialRequest financialRequest);
}
