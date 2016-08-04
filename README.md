# metapi-util-scala

metapi-util-scala provides some basic functionality that is used across all
modules in the API.

## BasicJsonFormat

Provides header elements + some common readers and writers for the standard
Json format.

## Send mail (Deprecated)
Provides a simple way to send emails.

Ex.

```scala
import no.met.mail._
import scala.util.{Success, Failure}

object test {
  import Mail._

  implicit val config = Config(emailHost=Some("smtp.host"),emailPort=Some25))

  val res = sendPlain(from="from@me", to=Seq("to@you"), cc=Seq("cc@some"),
    subject="The subject", message="Some message")

  res match {
    case Success(id) => println(s"Sendt email with id=$id")
    case Failure(e) => println(s"Failed: ${e.getMessage}")
  }
}
```
