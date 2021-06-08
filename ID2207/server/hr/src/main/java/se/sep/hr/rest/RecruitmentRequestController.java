package se.sep.hr.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import se.sep.common.domain.model.SepId;
import se.sep.hr.application.RecruitmentRequestService;
import se.sep.hr.domain.model.RecruitmentRequest;
import se.sep.hr.domain.model.Status;
import se.sep.hr.rest.dto.NewRecruitmentRequest;
import se.sep.hr.rest.dto.RecruitmentRequestDto;
import se.sep.hr.rest.mappers.RecruitmentRequestMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
public class RecruitmentRequestController {
  @Autowired
  private RecruitmentRequestMapper mapper;

  @Autowired
  private RecruitmentRequestService recruitmentRequestService;

  @RequestMapping(value = "/api/hr/recruitment/requests", method = RequestMethod.POST)
  public CompletableFuture<RecruitmentRequestDto> createRecruitmentRequest(@RequestBody NewRecruitmentRequest newRecruitmentRequest) {
    RecruitmentRequest recruitmentRequest = mapper.toRecruitmentRequest(newRecruitmentRequest);

    return recruitmentRequestService.createRecruitmentRequest(recruitmentRequest)
      .thenApply(mapper::fromRecruitmentRequest);
  }

  @RequestMapping(value = "/api/hr/recruitment/requests", method = RequestMethod.GET)
  public CompletableFuture<List<RecruitmentRequestDto>> getActiveRecruitmentRequests() {
    return recruitmentRequestService.findActiveRecruitmentRequests()
      .thenApply(requests -> requests.stream().map(mapper::fromRecruitmentRequest).collect(Collectors.toList()));
  }

  @RequestMapping(value = "/api/hr/recruitment/requests/{sepId}/status/{status}", method = RequestMethod.PUT)
  public CompletableFuture<RecruitmentRequestDto> updateRecruitmentRequestStatus(@PathVariable SepId sepId, @PathVariable Status status) {
    return recruitmentRequestService.updateRecruitmentRequestStatus(sepId, status)
      .thenApply(mapper::fromRecruitmentRequest);
  }
}
