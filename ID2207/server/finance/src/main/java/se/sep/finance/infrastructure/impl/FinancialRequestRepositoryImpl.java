package se.sep.finance.infrastructure.impl;


import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import se.sep.common.domain.model.SepId;
import se.sep.finance.domain.model.FinancialRequest;
import se.sep.finance.domain.model.Status;
import se.sep.finance.infrastructure.FinancialRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

@Repository
public class FinancialRequestRepositoryImpl implements FinancialRequestRepository {

  @Autowired
  MongoCollection<FinancialRequest> collection;

  @Override
  public CompletableFuture<FinancialRequest> create(FinancialRequest financialRequest) {
    CompletableFuture<FinancialRequest> future = new CompletableFuture<>();
    collection
      .insertOne(financialRequest, (result, throwable) -> {
        if (Objects.nonNull(throwable)) {
          future.completeExceptionally(throwable);
        } else {
          future.complete(financialRequest);
        }
      });

    return future;
  }

  @Override
  public CompletableFuture<FinancialRequest> update(SepId sepId, FinancialRequest financialRequest) {
    CompletableFuture<FinancialRequest> future = new CompletableFuture<>();

    collection
      .replaceOne(eq("sepId", sepId), financialRequest, (result, throwable) -> {
        if (Objects.nonNull(throwable)) {
          future.completeExceptionally(throwable);
        } else {
          future.complete(financialRequest);
        }
      });

    return future;
  }

  @Override
  public CompletableFuture<List<FinancialRequest>> findByStatus(Status status) {
    CompletableFuture<List<FinancialRequest>> future = new CompletableFuture<>();
    List<FinancialRequest> list = new ArrayList<>();

    collection.find(eq("status", status.name()))
      .projection(Projections.excludeId())
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
  public CompletableFuture<FinancialRequest> updateStatus(SepId sepId, Status status) {
    CompletableFuture<FinancialRequest> future = new CompletableFuture<>();
    collection.findOneAndUpdate(eq("sepId", sepId),
      combine(
        set("status", status.name()),
        set("lastModifiedAt", LocalDateTime.now())
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
}
