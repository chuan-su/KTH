package se.sep.common.lib;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import se.sep.common.domain.model.SepId;

import java.io.IOException;

public class SepIdSerializer extends StdSerializer<SepId> {

  public SepIdSerializer() {
    this(null);
  }

  protected SepIdSerializer(Class<SepId> t) {
    super(t);
  }

  @Override
  public void serialize(SepId value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(value.toString());
  }
}
