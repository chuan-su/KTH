package se.sep.production.rest.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import se.sep.production.domain.model.Project;
import se.sep.production.rest.dto.NewProject;
import se.sep.production.rest.dto.ProjectDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {

  @InheritInverseConfiguration
  ProjectDto fromProject(Project project);

  Project toProject(NewProject newProject);
}
