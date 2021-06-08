package se.sep.hr.infrastructure.impl;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import se.sep.common.domain.model.SepId;
import se.sep.hr.domain.model.RecruitmentRequest;
import se.sep.hr.domain.model.Status;
import se.sep.hr.infrastructure.RecruitmentRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

@Repository
public class RecruitmentRequestRepositoryImpl implements RecruitmentRequestRepository {

  @Autowired
  private MongoCollection<RecruitmentRequest> collection;

  @Override
  public CompletableFuture<RecruitmentRequest> create(RecruitmentRequest recruitmentRequest) {
    CompletableFuture<RecruitmentRequest> future = new CompletableFuture<>();
    collection
      .insertOne(recruitmentRequest, (result, throwable) -> {
        if (Objects.nonNull(throwable)) {
          future.completeExceptionally(throwable);
        } else {
          future.complete(recruitmentRequest);
        }
      });

    return future;
  }

  @Override
  public CompletableFuture<List<RecruitmentRequest>> findByStatus(Status status) {
    CompletableFuture<List<RecruitmentRequest>> future = new CompletableFuture<>();
    List<RecruitmentRequest> list = new ArrayList<>();

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
  public CompletableFuture<RecruitmentRequest> updateStatus(SepId sepId, Status status) {
    CompletableFuture<RecruitmentRequest> future = new CompletableFuture<>();
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
