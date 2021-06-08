package se.sep.hr.application;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sep.common.domain.model.SepId;
import se.sep.hr.application.impl.RecruitmentRequestServiceImpl;
import se.sep.hr.domain.model.RecruitmentRequest;
import se.sep.hr.domain.model.Status;
import se.sep.hr.infrastructure.RecruitmentRequestRepository;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecruitmentRequestServiceTest {

  @Mock
  private RecruitmentRequestRepository recruitmentRequestRepository;

  @InjectMocks
  private RecruitmentRequestService recruitmentRequestService = new RecruitmentRequestServiceImpl();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCreateRecruitmentRequest() {
    // Given
    when(recruitmentRequestRepository.create(any(RecruitmentRequest.class))).thenReturn(CompletableFuture.completedFuture(new RecruitmentRequest()));

    // When
    recruitmentRequestService.createRecruitmentRequest(new RecruitmentRequest()).join();

    // Then
    ArgumentCaptor<RecruitmentRequest> captor = ArgumentCaptor.forClass(RecruitmentRequest.class);
    verify(recruitmentRequestRepository).create(captor.capture());
    assertEquals(Status.OPEN, captor.getValue().getStatus());
  }

  @Test
  public void testUpdateFinancialRequestStatus() {
    // Given
    SepId recruitmentRequestId = SepId.create();

    when(recruitmentRequestRepository.updateStatus(any(SepId.class), any(Status.class))).thenReturn(CompletableFuture.completedFuture(new RecruitmentRequest()));

    // When
    recruitmentRequestService.updateRecruitmentRequestStatus(recruitmentRequestId, Status.COMPLETE).join();

    // Then
    verify(recruitmentRequestRepository).updateStatus(eq(recruitmentRequestId), eq(Status.COMPLETE));
  }

  @Test
  public void testFindByStatus() {
    // Given
    when(recruitmentRequestRepository.findByStatus(any(Status.class))).thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));

    // When
    recruitmentRequestService.findActiveRecruitmentRequests().join();

    // Then
    verify(recruitmentRequestRepository).findByStatus(eq(Status.OPEN));
  }
}
