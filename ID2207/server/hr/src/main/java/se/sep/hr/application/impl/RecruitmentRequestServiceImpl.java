package se.sep.hr.application.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.sep.common.domain.model.SepId;
import se.sep.hr.application.RecruitmentRequestService;
import se.sep.hr.domain.model.RecruitmentRequest;
import se.sep.hr.domain.model.Status;
import se.sep.hr.infrastructure.RecruitmentRequestRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RecruitmentRequestServiceImpl implements RecruitmentRequestService {
  @Autowired
  private RecruitmentRequestRepository recruitmentRequestRepository;

  @Override
  public CompletableFuture<RecruitmentRequest> createRecruitmentRequest(RecruitmentRequest recruitmentRequest) {
    recruitmentRequest.setStatus(Status.OPEN);
    return recruitmentRequestRepository.create(recruitmentRequest);
  }

  @Override
  public CompletableFuture<List<RecruitmentRequest>> findActiveRecruitmentRequests() {
    return recruitmentRequestRepository.findByStatus(Status.OPEN);
  }

  @Override
  public CompletableFuture<RecruitmentRequest> updateRecruitmentRequestStatus(SepId sepId, Status status) {
    return recruitmentRequestRepository.updateStatus(sepId, status);
  }
}
