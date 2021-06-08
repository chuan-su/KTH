package se.sep.production.application;

import se.sep.common.domain.model.SepId;
import se.sep.production.domain.model.Project;
import se.sep.production.domain.model.Task;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProjectService {
  CompletableFuture<List<Project>> getProjectList();

  CompletableFuture<Project> createProject(Project project);
  CompletableFuture<Project> addTasks(SepId projectId, List<Task> task);
  CompletableFuture<List<Project>> findProjectTasksByUserId(SepId userId);
}
