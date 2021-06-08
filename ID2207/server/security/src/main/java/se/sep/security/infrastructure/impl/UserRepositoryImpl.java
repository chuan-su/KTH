package se.sep.security.infrastructure.impl;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import se.sep.common.domain.model.SepId;
import se.sep.security.domain.model.Team;
import se.sep.security.domain.model.User;
import se.sep.security.infrastructure.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

@Repository
public class UserRepositoryImpl implements UserRepository {

  @Autowired
  MongoCollection<User> collection;

  @Override
  public CompletableFuture<User> findByUserName(String username) {
    CompletableFuture<User> future = new CompletableFuture();
    collection.find(eq("username", username))
      .first(((result, throwable) -> {
        if (Objects.nonNull(throwable)) {
          future.completeExceptionally(throwable);
        } else {
          future.complete(result);
        }
      }));
    return future;
  }

  @Override
  public CompletableFuture<List<User>> findByTeam(Team team) {
    CompletableFuture<List<User>> future = new CompletableFuture();
    List<User> list = new ArrayList<>();
    collection.find(eq("team", team.name()))
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
  public CompletableFuture<User> findByAuthToken(String token) {
    CompletableFuture<User> future = new CompletableFuture();
    collection.find(eq("authToken", token))
      .first(((result, throwable) -> {
        if (Objects.nonNull(throwable)) {
          future.completeExceptionally(throwable);
        } else {
          future.complete(result);
        }
      }));
    return future;
  }

  @Override
  public CompletableFuture<User> updateUserAuthToken(SepId userId, String token) {
    CompletableFuture<User> future = new CompletableFuture<>();
    collection.findOneAndUpdate(eq("sepId", userId),
      combine(set("authToken", token)),
      new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
      ((result, throwable) -> {
        if (Objects.nonNull(throwable)) {
          future.completeExceptionally(throwable);
        } else {
          future.complete(result);
        }
      }));
    return future;
  }

  @Override
  public CompletableFuture<User> createUser(User user) {
    CompletableFuture<User> future = new CompletableFuture<>();
    collection.insertOne(user, (result, throwable) -> {
      if (Objects.nonNull(throwable)) {
        future.completeExceptionally(throwable);
      } else {
        future.complete(user);
      }
    });
    return future;
  }
}
