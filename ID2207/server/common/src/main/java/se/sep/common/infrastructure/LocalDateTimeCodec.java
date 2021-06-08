package se.sep.common.infrastructure;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static java.lang.String.format;

public class LocalDateTimeCodec implements Codec<LocalDateTime> {

  @Override
  public LocalDateTime decode(final BsonReader reader, final DecoderContext decoderContext) {
    return Instant.ofEpochMilli(reader.readDateTime()).atZone(ZoneOffset.UTC).toLocalDateTime();
  }

  /**
   * {@inheritDoc}
   * <p>Converts the {@code LocalDateTime} to {@link ZoneOffset#UTC} via {@link LocalDateTime#toInstant(ZoneOffset)}.</p>
   * @throws CodecConfigurationException if the LocalDateTime cannot be converted to a valid Bson DateTime.
   */
  @Override
  public void encode(final BsonWriter writer, final LocalDateTime value, final EncoderContext encoderContext) {
    try {
      writer.writeDateTime(value.toInstant(ZoneOffset.UTC).toEpochMilli());
    } catch (ArithmeticException e) {
      throw new CodecConfigurationException(format("Unsupported LocalDateTime value '%s' could not be converted to milliseconds: %s",
        value, e.getMessage()), e);
    }
  }

  @Override
  public Class<LocalDateTime> getEncoderClass() {
    return LocalDateTime.class;
  }
}