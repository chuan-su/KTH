package se.sep.production.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import se.sep.common.domain.model.SepId;
import se.sep.production.application.ProjectService;
import se.sep.production.domain.model.Project;
import se.sep.production.domain.model.Task;
import se.sep.production.rest.dto.NewProject;
import se.sep.production.rest.dto.NewTask;
import se.sep.production.rest.dto.ProjectDto;
import se.sep.production.rest.mappers.ProjectMapper;
import se.sep.production.rest.mappers.TaskMapper;
import se.sep.security.application.AuthService;
import se.sep.security.domain.model.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

@RestController
public class ProjectController {
  @Autowired
  private ProjectMapper projectMapper;

  @Autowired
  private TaskMapper taskMapper;

  @Autowired
  private AuthService authService;

  @Autowired
  private ProjectService projectService;

  @RequestMapping(value = "/api/projects", method = RequestMethod.GET)
  public CompletableFuture<List<ProjectDto>> getAllProjects() {
    return projectService.getProjectList()
      .thenApply(projects -> projects.stream().map(projectMapper::fromProject).collect(toList()));
  }

  @RequestMapping(value = "/api/projects", method = RequestMethod.POST)
  public CompletableFuture<ProjectDto> createProject(@RequestBody NewProject newProject) {
    User currentUser = authService.getUserFromCurrentSession();

    Project project = projectMapper.toProject(newProject);

    project.setCreatedBy(currentUser.getSepId());

    return projectService.createProject(project)
      .thenApply(projectMapper::fromProject);
  }

  @RequestMapping(value = "/api/projects/{projectId}/tasks", method = RequestMethod.POST)
  public CompletableFuture<ProjectDto> addTasks(@PathVariable SepId projectId, @RequestBody List<NewTask> newTasks) {
    List<Task> tasks = newTasks.stream().map(taskMapper::toTask).collect(toList());

    return projectService.addTasks(projectId, tasks).thenApply(projectMapper::fromProject);
  }

  @RequestMapping(value = "/api/tasks/me", method = RequestMethod.GET)
  public CompletableFuture<List<ProjectDto>> getMyTasks() {
    User currentUser = authService.getUserFromCurrentSession();

    return projectService.findProjectTasksByUserId(currentUser.getSepId())
      .thenApply(projects -> projects.stream().map(projectMapper::fromProject).collect(toList()));
  }
}
