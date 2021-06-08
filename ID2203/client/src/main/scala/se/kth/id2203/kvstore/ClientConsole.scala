/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.kvstore

import com.larskroll.common.repl._
import com.typesafe.scalalogging.StrictLogging;
import org.apache.log4j.Layout
import util.log4j.ColoredPatternLayout;
import fastparse.all._
import concurrent.Await
import concurrent.duration._

object ClientConsole {
  // Better build this statically. Has some overhead (building a lookup table).
  val simpleStr = P(CharsWhileIn(('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z'), 1).!);
  val colouredLayout = new ColoredPatternLayout("%d{[HH:mm:ss,SSS]} %-5p {%c{1}} %m%n");
}

class ClientConsole(val service: ClientService) extends CommandConsole with ParsedCommands with StrictLogging {
  import ClientConsole._;

  override def layout: Layout = colouredLayout;
  override def onInterrupt(): Unit = exit();

  val opCommand = parsed(P("op" ~ " " ~ simpleStr), usage = "op <key>", descr = "Executes an op for <key>.") { key =>
    println(s"Op with $key");

    val fr = service.op(key);
    out.println("Operation sent! Awaiting response...");
    try {
      val r = Await.result(fr, 5.seconds);
      out.println("Operation complete! Response was: " + r.status);
    } catch {
      case e: Throwable => logger.error("Error during op.", e);
    }
  }

  val getCommand = parsed(P("get" ~ " " ~ simpleStr), usage = "get <key>", descr = "Executes an get for <key>.") { key =>
    println(s"Get with $key");

    val fr = service.get(key);
    out.println("Operation sent! Awaiting response...");
    try {
      val r = Await.result(fr, 5.seconds);
      r.value match {
        case Some(v) =>
          out.println("Operation complete! Response was: " + r.status + " " + v.toString)
        case None =>
          out.println("Operation complete! Response was: " + r.status)
      }
    } catch {
      case e: Throwable => logger.error("Error during op.", e);
    }
  }

  val putCommand = parsed(P("put" ~ " " ~ simpleStr ~ " " ~ simpleStr), usage = "put <key> <value>", descr = "Executes an put for <key> <value>.") { kv =>
    println(s"Put with ${kv._1} ${kv._2}");

    val fr = service.put(kv._1, kv._2);
    out.println("Operation sent! Awaiting response...");
    try {
      val r = Await.result(fr, 5.seconds);
      out.println("Operation complete! Response was: " + r.status);
    } catch {
      case e: Throwable => logger.error("Error during op.", e);
    }
  }

  val casCommand = parsed(P("cas" ~ " " ~ simpleStr ~ " " ~ simpleStr ~ " " ~ simpleStr), usage = "cas <key> <refValue> <newValue>", descr = "Executes an cats for <refValue> <newValue>.") { kv =>
    println(s"Cas with ${kv._1} ${kv._2} ${kv._3}");

    val fr = service.cas(kv._1, kv._2, kv._3);
    out.println("Operation sent! Awaiting response...");
    try {
      val r = Await.result(fr, 5.seconds);
      out.println("Operation complete! Response was: " + r.status);
    } catch {
      case e: Throwable => logger.error("Error during op.", e);
    }
  };

}
