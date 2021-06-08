package se.sep.production.application.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.sep.common.domain.model.SepId;
import se.sep.production.application.ProjectService;
import se.sep.production.domain.model.Project;
import se.sep.production.domain.model.Task;
import se.sep.production.infrastructure.ProjectRepository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

  @Autowired
  private ProjectRepository projectRepository;

  @Override
  public CompletableFuture<List<Project>> getProjectList() {
    return projectRepository.findAll();
  }

  @Override
  public CompletableFuture<Project> createProject(Project project) {
    return projectRepository.create(project);
  }

  @Override
  public CompletableFuture<Project> addTasks(SepId projectId, List<Task> tasks) {
    return projectRepository.addTasks(projectId, tasks);
  }

  @Override
  public CompletableFuture<List<Project>> findProjectTasksByUserId(SepId userId) {
    return projectRepository.findByTaskAssigneeId(userId)
      .thenApply(projects -> projects.stream()
        .filter(project -> Objects.nonNull(project.getTasks()))
        .map(project -> {
          List<Task> myTasks = project.getTasks().stream()
            .filter(task -> Objects.equals(userId, task.getAssignedUserId()))
            .collect(Collectors.toList());
          project.setTasks(myTasks);
          return project;
        })
        .filter(project -> !project.getTasks().isEmpty())
        .collect(Collectors.toList()));
  }
}
