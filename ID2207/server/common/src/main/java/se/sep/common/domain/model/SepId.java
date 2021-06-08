package se.sep.common.domain.model;

import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.Optional;

public class SepId {
  private String sepIdStr;

  private SepId(String sepIdStr) {
    this.sepIdStr = sepIdStr;
  }

  @Override
  public String toString() {
    return sepIdStr;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof SepId)) {
      return false;
    }
    SepId sepId = (SepId) o;
    return Objects.equals(sepIdStr, sepId.toString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(sepIdStr);
  }

  public static SepId create() {
    return new SepId(ObjectId.get().toHexString());
  }

  public static SepId valueOf(String sepIdStr) {
    return Optional.ofNullable(sepIdStr)
      .filter(ObjectId::isValid)
      .map(SepId::new)
      .orElseThrow(() -> new IllegalArgumentException("Not a valid SEP ID"));
  }
}
