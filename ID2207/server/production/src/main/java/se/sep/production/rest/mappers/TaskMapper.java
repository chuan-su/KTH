package se.sep.production.rest.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import se.sep.production.domain.model.Task;
import se.sep.production.rest.dto.NewTask;

import se.sep.production.rest.dto.TaskDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

  @InheritInverseConfiguration
  TaskDto fromTask(Task task);

  Task toTask(NewTask task);
}
