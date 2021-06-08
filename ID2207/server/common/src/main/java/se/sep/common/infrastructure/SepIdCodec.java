package se.sep.common.infrastructure;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import se.sep.common.domain.model.SepId;

public class SepIdCodec implements Codec<SepId> {

  @Override
  public SepId decode(BsonReader reader, DecoderContext decoderContext) {
    return SepId.valueOf(reader.readString());
  }

  @Override
  public void encode(BsonWriter writer, SepId value, EncoderContext encoderContext) {
    writer.writeString(value.toString());
  }

  @Override
  public Class<SepId> getEncoderClass() {
    return SepId.class;
  }
}
