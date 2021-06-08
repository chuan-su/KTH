package ir;

import org.junit.Test;
import static org.junit.Assert.*;

public class HashedIndexTest {

  @Test
  public void testInsert() {
    // Given
    HashedIndex index = new HashedIndex();
    // When
    index.insert("test", 1, 10);
    // Then
    PostingsList result = index.getPostings("test");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(1, result.get(0).docID);
    assertEquals(10, result.get(0).positions.get(0).intValue());
  }

  @Test
  public void testInsertWhenTermExists() {
    // Given
    HashedIndex index = new HashedIndex();
    // When
    index.insert("test", 1, 10);
    index.insert("test", 2, 11);

    // Then
    PostingsList result = index.getPostings("test");

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(1, result.get(0).docID);
    assertEquals(10, result.get(0).positions.get(0).intValue());

    assertEquals(2, result.get(1).docID);
    assertEquals(11, result.get(1).positions.get(0).intValue());
  }

  @Test
  public void testInsertWhenDocExists() {
    // Given
    HashedIndex index = new HashedIndex();
    // When
    index.insert("test", 1, 10);
    index.insert("test", 1, 11);

    // Then
    PostingsList result = index.getPostings("test");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(1, result.get(0).docID);
    assertEquals(10, result.get(0).positions.get(0).intValue());
    assertEquals(11, result.get(0).positions.get(1).intValue());
  }
}
