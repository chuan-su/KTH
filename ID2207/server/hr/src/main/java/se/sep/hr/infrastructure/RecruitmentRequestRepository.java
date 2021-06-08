package se.sep.hr.infrastructure;

import se.sep.common.domain.model.SepId;
import se.sep.hr.domain.model.RecruitmentRequest;
import se.sep.hr.domain.model.Status;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RecruitmentRequestRepository {

  CompletableFuture<RecruitmentRequest> create(RecruitmentRequest recruitmentRequest);
  CompletableFuture<List<RecruitmentRequest>> findByStatus(Status status);
  CompletableFuture<RecruitmentRequest> updateStatus(SepId sepId, Status status);
}
