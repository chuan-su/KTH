package se.sep.security.util;

import org.apache.tomcat.util.codec.binary.Base64;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TokenGenerator {

  public static String generateToken(String secret) throws NoSuchAlgorithmException {

    String timestamp = LocalDateTime.now().format(
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

    String concat = secret + timestamp;
    MessageDigest digest = MessageDigest.getInstance("SHA1");

    byte[] hash = digest.digest(concat.getBytes(Charset.forName("UTF-8")));
    String token = new String(Base64.encodeBase64URLSafeString(hash));

    return token;
  }
}
