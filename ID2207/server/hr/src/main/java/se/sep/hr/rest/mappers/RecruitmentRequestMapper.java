package se.sep.hr.rest.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import se.sep.hr.domain.model.RecruitmentRequest;
import se.sep.hr.rest.dto.NewRecruitmentRequest;
import se.sep.hr.rest.dto.RecruitmentRequestDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecruitmentRequestMapper {

  @InheritInverseConfiguration
  RecruitmentRequestDto fromRecruitmentRequest(RecruitmentRequest recruitmentRequest);

  RecruitmentRequest toRecruitmentRequest(NewRecruitmentRequest newRecruitmentRequest);
}
