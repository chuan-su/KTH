package se.sep.production.application;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sep.common.domain.model.SepId;
import se.sep.production.application.impl.ProjectServiceImpl;
import se.sep.production.domain.model.Project;
import se.sep.production.domain.model.Task;
import se.sep.production.infrastructure.ProjectRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @InjectMocks
  private ProjectService projectService = new ProjectServiceImpl();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCreateProject() {
    // Given
    Project project = new Project();
    when(projectRepository.create(any(Project.class))).thenReturn(CompletableFuture.completedFuture(project));

    // When
    projectService.createProject(project).join();

    // Then
    verify(projectRepository).create(eq(project));
  }

  @Test
  public void testAddTasks() {
    // Given
    SepId projectId = SepId.create();
    List<Task> taskList = new ArrayList();
    when(projectRepository.addTasks(any(SepId.class), any(List.class))).thenReturn(CompletableFuture.completedFuture(new Project()));

    // When
    projectService.addTasks(projectId, taskList).join();

    // Then
    verify(projectRepository).addTasks(eq(projectId), any(List.class));
  }

  @Test
  public void testGetProjectList() {
    // Given
    when(projectRepository.findAll()).thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));

    // When
    projectService.getProjectList().join();

    // Then
    verify(projectRepository).findAll();
  }

  @Test
  public void testFindProjectTasksByUserId() {
    // Given
    SepId userId = SepId.create();
    List<Project> projects = Arrays.asList(new Project(), new Project(), new Project(), new Project());

    List<Task> tasks1 = IntStream.range(0, 10)
      .mapToObj(i -> {
        Task task = new Task();
        if ((i >> 1) == 0) {
          task.setAssignedUserId(userId);
        } else {
          task.setAssignedUserId(SepId.create());
        }
        return task;
      })
      .collect(Collectors.toList());

    List<Task> tasks2 = IntStream.range(0, 10)
      .mapToObj(i -> {
        Task task = new Task();
        task.setAssignedUserId(SepId.create());
        return task;
      })
      .collect(Collectors.toList());

    List<Task> tasks3 = IntStream.range(0, 10)
      .mapToObj(i -> {
        Task task = new Task();
        task.setAssignedUserId(userId);
        return task;
      })
      .collect(Collectors.toList());

    projects.get(0).setTasks(tasks1);
    projects.get(1).setTasks(tasks2);
    projects.get(2).setTasks(tasks3);

    when(projectRepository.findByTaskAssigneeId(eq(userId))).thenReturn(CompletableFuture.completedFuture(projects));

    // When
    List<Project> result = projectService.findProjectTasksByUserId(userId).join();

    // Then
    result.forEach(project -> {
      if (Objects.nonNull(project)) {
        project.getTasks().forEach(task -> assertEquals(userId, task.getAssignedUserId()));
      }
    });
  }

}
