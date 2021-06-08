package se.sep.common.lib;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import se.sep.common.domain.model.SepId;

import java.io.IOException;

public class SepIdDeserializer extends StdDeserializer<SepId> {

  public SepIdDeserializer() {
    this(null);
  }
  protected SepIdDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public SepId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    return SepId.valueOf(p.getValueAsString());
  }
}
