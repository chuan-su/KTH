package se.sep.production.infrastructure.impl;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import se.sep.common.domain.model.SepId;
import se.sep.production.domain.model.Project;
import se.sep.production.domain.model.Task;
import se.sep.production.infrastructure.ProjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class ProjectRepositoryImpl implements ProjectRepository {

  @Autowired
  MongoCollection<Project> collection;

  @Override
  public CompletableFuture<List<Project>> findAll() {
    CompletableFuture<List<Project>> future = new CompletableFuture<>();
    List<Project> list = new ArrayList<>();

    collection.find()
      .forEach(list::add, (result, throwable) -> {
        if (Objects.nonNull(throwable)) {
          future.completeExceptionally(throwable);
        } else {
          future.complete(list);
        }
      });
    return future;
  }

  @Override
  public CompletableFuture<Project> create(Project project) {
    CompletableFuture<Project> future = new CompletableFuture<>();

    collection.insertOne(project, (result, throwable) -> {
      if (Objects.nonNull(throwable)){
        future.completeExceptionally(throwable);
      } else {
        future.complete(project);
      }
    });
    return future;
  }

  @Override
  public CompletableFuture<Project> addTasks(SepId projectId, List<Task> tasks) {
    CompletableFuture<Project> future = new CompletableFuture<>();
    collection.findOneAndUpdate(
      eq("sepId", projectId),
      Updates.pushEach("tasks", tasks),
      new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
      (result, throwable) -> {
        if (Objects.nonNull(throwable)){
          future.completeExceptionally(throwable);
        } else {
          future.complete(result);
        }
      });
    return future;
  }

  @Override
  public CompletableFuture<List<Project>> findByTaskAssigneeId(SepId userId) {
    CompletableFuture<List<Project>> future = new CompletableFuture<>();
    List<Project> list = new ArrayList<>();

    collection.find(eq("tasks.assignedUserId", userId))
      .forEach(list::add, (result, throwable) -> {
        if (Objects.nonNull(throwable)) {
          future.completeExceptionally(throwable);
        } else {
          future.complete(list);
        }
      });

    return future;
  }
}
