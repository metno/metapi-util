/*
    MET-API

    Copyright (C) 2014 met.no
    Contact information:
    Norwegian Meteorological Institute
    Box 43 Blindern
    0313 OSLO
    NORWAY
    E-mail: met-api@met.no

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
    MA 02110-1301, USA
*/

package no.met.mail

import org.specs2._
import org.specs2.runner._
import org.junit.runner._
import org.junit.runner.RunWith

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Files

import scala.util.{ Try, Success, Failure }
import org.subethamail.wiser._
import javax.mail.internet.MimeMessage
import javax.mail.Multipart
import scala.collection.JavaConversions._
import java.io.InputStream
import scala.concurrent.ExecutionContext.Implicits.global

//scalastyle:off public.methods.have.type
//scalastyle:off regex

@RunWith(classOf[JUnitRunner])
class MailSpec extends mutable.Specification with specification.BeforeAfterAll {
  sequential

  val fromAdr = "from@me"
  val toAdr = Seq("to@you")
  val to2Adr = Seq("to@you", "to@you2")

  val ccAdr = Seq("to@some")

  val debug = true
  val port = findFreePort()
  val wiser = new Wiser()
  implicit val config = Mail.Config(Some("localhost"), port)

  def findFreePort(): Option[Long] = {
    import java.net.ServerSocket
    Try {
      new ServerSocket(0);
    } match {
      case Success(s) =>
        val port = s.getLocalPort()
        Try(s.close()) //Ignore exception
        Some(port)
      case Failure(e) => println("Getting free port failed: " + e.getMessage); None
    }
  }

  def debugPrint(msg: WiserMessage, subject: String): Unit = {
    println(s"---------BEGIN: $subject -------------------------------")
    msg.getMimeMessage.getContent match {
      case mp: Multipart =>
        println(s"Multipart: ${mp.getCount}")
        for (i <- 0 until mp.getCount) {
          val part = mp.getBodyPart(i)
          println(s"    part: ${i} Content-type: ${part.getContentType}")
        }
      case s: String => println(s"Plain: ${s}")
      case is: InputStream => println(s"InputStream: Unknown")
      case _ => println("Unknown: Content")
    }
    println(s"Data: ${new String(msg.getData)}")
    println(s"---------END: $subject -------------------------------")
  }

  def checkHeader(header: String, v: String, msg: WiserMessage): Boolean =
    checkHeader(header, v, msg.getMimeMessage)

  def checkHeader(header: String, v: String, msg: MimeMessage): Boolean =
    !msg.getHeader(header).filter(_.indexOf(v) >= 0).isEmpty

  def findMsgId(id: String): Option[WiserMessage] =
    wiser.getMessages.filter(_.getMimeMessage.getMessageID == id).headOption

  def beforeAll() {
    port must beSome
    wiser.setPort(port.get.toInt)
    wiser.start
  }

  def afterAll() {
    wiser.stop
  }

  def checkMail(id: String, subject: String, multiPartCount: Int,
    from: String = fromAdr, to: Seq[String] = toAdr, debug: Boolean = false)(implicit config: Mail.Config): Boolean = {
    def checkSubject(implicit msg: MimeMessage) = msg.getSubject == subject
    def checkAdr(h: String, adr: Seq[String])(implicit msg: MimeMessage) =
      adr.filter(checkHeader(h, _, msg)).size == adr.size
    def checkMultipart(implicit msg: MimeMessage) = multiPartCount == {
      msg.getContent match {
        case mp: Multipart => mp.getCount
        case _ => 0
      }
    }
    def checkBounce(msg: WiserMessage) =
      config.emailBounce map (msg.getEnvelopeSender.indexOf(_) >= 0) getOrElse (true)
    def checkReplyTo(msg: WiserMessage) =
      config.emailReplayto map (checkHeader("Reply-To", _, msg)) getOrElse (true)

    findMsgId(id) match {
      case Some(message) =>
        implicit val msg: MimeMessage = message.getMimeMessage()
        val list = checkMultipart :: checkSubject ::
          checkAdr("To", to) :: checkAdr("From", Seq(from)) ::
          checkBounce(message) :: checkReplyTo(message) :: Nil

        if (debug) debugPrint(message, subject)

        list forall (_ == true)
      case _ => false
    }
  }

  "Sendmail" should {

    "send plain emails" in {
      val ret = Mail.sendPlain(from = fromAdr, to = toAdr, subject = "Test1", message = "Test message")
      ret must beSuccessfulTry.which(checkMail(_, "Test1", 0))
    }

   "send plain emails with multiple recipients" in {
      val ret = Mail.sendPlain(from = fromAdr, to = to2Adr, subject = "Test1a", message = "Test message")
      ret must beSuccessfulTry.which(checkMail(_, "Test1a", 0, to=to2Adr))
    }

    "send simple emails with attachments" in {
      val file = FileSystems.getDefault.getPath("src/test/test.dat")
      val ret = Mail.sendWithAttachments(from = fromAdr, to = toAdr, subject = "Test2", message = "Message with attachments.",
        attachments = Seq(file))

      ret must beSuccessfulTry.which(checkMail(_, "Test2", 2))
    }

    "send html formatted emails" in {
      val html =
        """<html><body><h1>Hello</h1>world</body></html>"""
      val ret = Mail.send(from = fromAdr, to = toAdr, subject = "Test3",
        message = "Your email client does not support HTML messages", htmlMessage = Some(html))

      ret must beSuccessfulTry.which(checkMail(_, "Test3", 2))
    }

    "send html formatted emails with attachments" in {
      val file = FileSystems.getDefault.getPath("src/test/test.dat")
      val html =
        """<html><body><h1>Hello</h1>world</body></html>"""
      val ret = Mail.send(from = fromAdr, to = toAdr, subject = "Test4",
        message = "Your email client does not support HTML messages", htmlMessage = Some(html),
        attachments = Seq(file))

      ret must beSuccessfulTry.which(checkMail(_, "Test4", 3))
    }

    "send simple emails with cc, replyTo and bounce set" in {
      implicit val config = Mail.Config(Some("localhost"), port, Some("bounce@met.no"), Some("replayto@met.no"))
      val ret = Mail.sendPlain(from = fromAdr, to = toAdr,
        cc = Seq("cc@met.no"), subject = "Test5", message = "Test message")
      val msg = ret.map(findMsgId(_)).getOrElse(None)
      msg must beSome

      checkHeader("Reply-To", "replayto@met.no", msg.get) mustEqual true
      checkHeader("Cc", "cc@met.no", msg.get) mustEqual true

      ret must beSuccessfulTry.which(checkMail(_, "Test5", 0))
    }

    "send emails should fail if missing config emailHost" in {
      implicit val config = Mail.Config(None)
      val ret = Mail.sendPlain(from = fromAdr, to = toAdr, subject = "Test6", message = "Test message")
      ret must beFailedTry
    }

    "send emails asynchronously" in {
      val ret = Mail.sendAsync(from = fromAdr, to = toAdr, subject = "Test7", message = "Test message")
      ret map { _ must beSuccessfulTry } await
    }
  }
}
