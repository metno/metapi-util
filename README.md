# Utility classes and functions for the data.met.no MET API

metapi-util provides a number of basic utility classes and functionality that
is common to most of the modules in the MET API.

## Package no.met.data

This package provides basic functionality specific to the API code. This 
includes the BasicResponse class, and a number of constants used for 
responses. It also contains the BadRequestException, as well as the 
SourceSpecification and FieldSpecification objects which are used to parse
sources and fields query parameters respectively.

## Package no.met.geometry

This defines geometry objects and level objects for the data.met.no MET API.

## Package no.met.json

Provides the basic JSON formatting code for the data.met.no MET API. The
classes here contains some of the common readers and writers for the Json
format.

## Package no.met.mail (Deprecated)

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

This package is deprecated.

## Package no.met.time

This contains classes and functions for the handling of the time specification
in the data.met.no MET API.

