package se.sep.epr.infrastructure.impl;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import se.sep.common.domain.model.SepId;
import se.sep.epr.domain.model.Comment;
import se.sep.epr.domain.model.Epr;
import se.sep.epr.domain.model.Status;
import se.sep.epr.infrastructure.EprRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

@Repository
public class EprRepositoryImpl implements EprRepository {

  @Autowired
  private MongoCollection<Epr> collection;

  @Override
  public CompletableFuture<Epr> create(Epr epr) {
    CompletableFuture<Epr> future = new CompletableFuture<>();

    collection.insertOne(epr, (result, throwable) -> {
      if (Objects.nonNull(throwable)){
        future.completeExceptionally(throwable);
      } else {
        future.complete(epr);
      }
    });
    return future;
  }

  @Override
  public CompletableFuture<Epr> update(SepId eprId, Epr epr) {
    CompletableFuture<Epr> future = new CompletableFuture<>();
    collection.findOneAndUpdate(eq("sepId", eprId),
      combine(
        set("lastModifiedBy", epr.getLastModifiedBy()),
        set("preferences", epr.getPreferences()),
        set("lastModifiedAt", epr.getLastModifiedAt()),
        set("clientName", epr.getClientName()),
        set("numberOfAttendees", epr.getNumberOfAttendees()),
        set("plannedBudget", epr.getPlannedBudget()),
        set("eventType", epr.getEventType()),
        set("eventDateFrom", epr.getEventDateFrom()),
        set("eventDateTo", epr.getEventDateTo())
      ),
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
  public CompletableFuture<Epr> updateStatus(SepId eprId, Epr epr) {
    CompletableFuture<Epr> future = new CompletableFuture<>();
    collection.findOneAndUpdate(eq("sepId", eprId),
      combine(
        set("lastModifiedBy", epr.getLastModifiedBy()),
        set("status", epr.getStatus().name()),
        set("lastModifiedAt", epr.getLastModifiedAt())
      ),
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
  public CompletableFuture<Epr> updateFinance(SepId eprId, Epr epr) {
    CompletableFuture<Epr> future = new CompletableFuture<>();
    collection.findOneAndUpdate(eq("sepId", eprId),
      combine(
        set("lastModifiedBy", epr.getLastModifiedBy()),
        set("suggestedBudget", epr.getSuggestedBudget()),
        set("suggestedDiscount", epr.getSuggestedDiscount()),
        set("lastModifiedAt", epr.getLastModifiedAt())
      ),
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
  public CompletableFuture<Epr> findById(SepId eprId) {
    CompletableFuture<Epr> future = new CompletableFuture<>();
    collection.find(eq("sepId", eprId))
      .first((result, throwable) -> {
        if (Objects.nonNull(throwable)) {
          future.completeExceptionally(throwable);
        } else {
          future.complete(result);
        }
      });
    return future;
  }

  @Override
  public CompletableFuture<List<Epr>> findByStatus(Status status) {
    CompletableFuture<List<Epr>> future = new CompletableFuture<>();
    List<Epr> list = new ArrayList<>();
    collection.find(eq("status", status.name()))
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
  public CompletableFuture<List<Epr>> findAll() {
    CompletableFuture<List<Epr>> future = new CompletableFuture<>();
    List<Epr> list = new ArrayList<>();
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
  public CompletableFuture<Epr> addComment(SepId eprId, Comment comment) {
    CompletableFuture<Epr> future = new CompletableFuture<>();
    collection.findOneAndUpdate(
      eq("sepId", eprId),
      Updates.push("comments", comment),
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
}
