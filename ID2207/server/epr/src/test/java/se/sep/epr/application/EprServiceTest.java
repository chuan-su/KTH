package se.sep.epr.application;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import se.sep.common.domain.model.SepId;
import se.sep.epr.application.impl.DomainUserMapper;
import se.sep.epr.application.impl.EprServiceImpl;
import se.sep.epr.domain.model.Comment;
import se.sep.epr.domain.model.Epr;
import se.sep.epr.domain.model.Status;
import se.sep.epr.domain.model.User;
import se.sep.epr.infrastructure.EprRepository;
import se.sep.security.application.AuthService;
import se.sep.security.domain.model.Department;
import se.sep.security.domain.model.Role;

import java.util.concurrent.CompletableFuture;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EprServiceTest {
  @Mock
  private DomainUserMapper domainUserMapper;

  @Mock
  private EprRepository eprRepository;

  @Mock
  private AuthService authService;

  @InjectMocks
  private EprService eprService = new EprServiceImpl();

  private se.sep.security.domain.model.User user;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    user = new se.sep.security.domain.model.User();
    user.setDepartment(Department.CUSTOMER_SERVICE);
    user.setRole(Role.CUSTOMER_SERVICE_OFFICER);

    when(authService.getUserFromCurrentSession()).thenReturn(user);

    when(domainUserMapper.toUser(any(se.sep.security.domain.model.User.class)))
      .thenAnswer(invocation -> {
        se.sep.security.domain.model.User arg = invocation.getArgument(0);

        User user = new User();
        user.setDepartment(arg.getDepartment());
        user.setRole(arg.getRole());
        user.setUserId(arg.getSepId());
        user.setUsername(arg.getUsername());
        return user;
      });
  }

  @Test
  public void testCreateEpr() {
    // Given
    when(eprRepository.create(any(Epr.class)))
      .thenAnswer(invocation -> CompletableFuture.completedFuture(invocation.getArgument(0)));

    // When
    eprService.createEpr(new Epr()).join();

    // Then
    verify(authService).getUserFromCurrentSession();
    verify(domainUserMapper).toUser(any(se.sep.security.domain.model.User.class));

    ArgumentCaptor<Epr> captor = ArgumentCaptor.forClass(Epr.class);
    verify(eprRepository).create(captor.capture());

    assertEquals(Status.OPEN, captor.getValue().getStatus());
    assertEquals(user.getDepartment(), captor.getValue().getLastModifiedBy().getDepartment());
    assertEquals(user.getRole(), captor.getValue().getLastModifiedBy().getRole());
  }

  @Test
  public void testUpdateEpr() {
    // Given
    when(eprRepository.update(any(SepId.class), any(Epr.class)))
      .thenAnswer(invocation -> CompletableFuture.completedFuture(invocation.getArgument(0)));

    // When
    SepId eprId = SepId.create();
    eprService.updateEpr(eprId, new Epr()).join();

    // Then
    verify(authService).getUserFromCurrentSession();
    verify(domainUserMapper).toUser(any(se.sep.security.domain.model.User.class));

    ArgumentCaptor<Epr> captor = ArgumentCaptor.forClass(Epr.class);
    verify(eprRepository).update(eq(eprId),captor.capture());

    assertEquals(Status.OPEN, captor.getValue().getStatus());
    assertEquals(user.getDepartment(), captor.getValue().getLastModifiedBy().getDepartment());
    assertEquals(user.getRole(), captor.getValue().getLastModifiedBy().getRole());
  }

  @Test
  public void testAddComment() {
    // Given
    when(eprRepository.addComment(any(SepId.class), any(Comment.class)))
      .thenAnswer(invocation -> CompletableFuture.completedFuture(invocation.getArgument(0)));

    // When
    SepId eprId = SepId.create();
    eprService.addComment(eprId, new Comment()).join();

    // Then
    verify(authService).getUserFromCurrentSession();
    verify(domainUserMapper).toUser(any(se.sep.security.domain.model.User.class));

    ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
    verify(eprRepository).addComment(eq(eprId), captor.capture());

    assertEquals(user.getDepartment(), captor.getValue().getCommentedBy().getDepartment());
    assertEquals(user.getRole(), captor.getValue().getCommentedBy().getRole());
  }
}
