package se.sep.production.infrastructure;

import se.sep.common.domain.model.SepId;
import se.sep.production.domain.model.Project;
import se.sep.production.domain.model.Task;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProjectRepository {
  CompletableFuture<List<Project>> findAll();

  CompletableFuture<Project> create(Project project);
  CompletableFuture<Project> addTasks(SepId projectId, List<Task> task);
  CompletableFuture<List<Project>> findByTaskAssigneeId(SepId userId);
}
