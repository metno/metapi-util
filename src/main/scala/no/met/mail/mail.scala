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

/**
 * This email component use the apache commons email library.
 *  https://commons.apache.org/proper/commons-email/
 */

package no.met.mail

import org.apache.commons.mail._
import java.nio.file.Path
import scala.util.{ Try, Success, Failure }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.Collections.SetFromMap
import scala.util.Failure

object Mail {

  /**
   * Configuration for the email methods.
   */
  case class Config(val emailHost: Option[String],
    val emailPort: Option[Long] = Some(25), // scalastyle:ignore
    val emailBounce: Option[String] = None,
    val emailReplyto: Option[String] = None)

  /**
   *  An implicit that use application.conf to set the configuration
   *  parameters.
   */
  //  implicit val config = Config(Play.configuration.getString("email.host"),
  //    Play.configuration.getLong("email.smtp.port"),
  //    Play.configuration.getString("email.bounce"),
  //    Play.configuration.getString("email.replyto"))

  private def doSend(
    from: String, to: Seq[String], cc: Seq[String] = Seq.empty,
    subject: String, email: Email)(implicit config: Config): Try[String] = Try {
    import config._

    if (emailHost.isEmpty) {
      throw new Exception("email: No smtp server is configured. (email.host)")
    }

    email.setHostName(config.emailHost.get)

    emailPort foreach (p => email.setSmtpPort(p.toInt))
    email.setSubject(subject)
    email.setFrom(from)

    to foreach (email.addTo(_))
    cc foreach (email.addCc(_))
    emailBounce foreach (email.setBounceAddress(_))
    emailReplyto foreach (email.addReplyTo(_))
    email.send
  }

  /**
   *  Send an email. The email may be a plain text message or a html message.
   *  Any file attachments can be added. It is not wise to send
   *  attachments greater than 10Mb as many smtp-servers has constraints set on
   *  the size of the mail size.
   *
   *  @param from Who sends the email
   *  @param to The recipients of this mail
   *  @param cc cc to this recipients
   *  @param bcc bcc to this recipients
   *  @param subject What is the mail about
   *  @param message The message, in case this is an html message, this is the text
   *     that is shown if the email reader do not support html mails.
   *  @param htmlMessage An html formatted message
   *  @param attachments, paths to any attachments.
   *  @return Try[String], an message id on success and None on failure.
   */
  def send(
    from: String, to: Seq[String], cc: Seq[String] = Seq.empty,
    subject: String, message: String, htmlMessage: Option[String] = None,
    attachments: Seq[Path] = Seq.empty)(implicit config: Config): Try[String] = {

    def addAttachments(email: MultiPartEmail, attachments: Seq[Path]): MultiPartEmail = {
      attachments foreach { file =>
        val a = new EmailAttachment
        a.setDisposition(EmailAttachment.ATTACHMENT)
        a.setName(file.getFileName.toString)
        a.setPath(file.toAbsolutePath.toString)
        email.attach(a)
      }
      email
    }

    //Html messages is multipart messages.
    val email = htmlMessage match {
      case Some(html) =>
        val m = new HtmlEmail
        m.setHtmlMsg(html)
        m.setTextMsg(message)
        addAttachments(m, attachments)
      case _ =>
        val m = attachments match {
          case s if !s.isEmpty => addAttachments(new MultiPartEmail, s)
          case _ => new SimpleEmail
        }
        m.setMsg(message)
    }

    doSend(from, to, cc, subject, email)
  }

  /**
   * Send a plain message with attachments.
   * This is a convenient for 'send'
   * @see send
   */
  def sendWithAttachments(
    from: String, to: Seq[String], cc: Seq[String] = Seq.empty,
    subject: String, message: String, attachments: Seq[Path])(implicit config: Config): Try[String] = {
    send(from, to, cc, subject, message, None, attachments)
  }

  /**
   * Send a plain message.
   * This is a convenient for 'send'
   * @see send
   */
  def sendPlain(
    from: String, to: Seq[String], cc: Seq[String] = Seq.empty,
    subject: String, message: String)(implicit config: Config): Try[String] = {
    send(from, to, cc, subject, message, None, Seq.empty)
  }

  /**
   * Send an mail message asynchronously.
   * @see send
   */
  def sendAsync(
    from: String, to: Seq[String], cc: Seq[String] = Seq.empty,
    subject: String, message: String, htmlMessage: Option[String] = None,
    attachments: Seq[Path] = Seq.empty)(implicit config: Config): Future[Try[String]] = Future(send(from, to, cc, subject, message, htmlMessage, attachments))
}
