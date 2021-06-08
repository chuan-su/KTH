package se.sep.hr.application;

import se.sep.common.domain.model.SepId;
import se.sep.hr.domain.model.RecruitmentRequest;
import se.sep.hr.domain.model.Status;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RecruitmentRequestService {
  CompletableFuture<RecruitmentRequest> createRecruitmentRequest(RecruitmentRequest recruitmentRequest);
  CompletableFuture<List<RecruitmentRequest>> findActiveRecruitmentRequests();
  CompletableFuture<RecruitmentRequest> updateRecruitmentRequestStatus(SepId sepId, Status status);
}
