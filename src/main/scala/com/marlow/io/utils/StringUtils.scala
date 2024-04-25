package com.marlow.io.utils

import com.marlow.io.misc.Loggie
import com.marlow.io.model.{Image, MediaType, Pdf, Unknown}
import org.apache.commons.codec.binary.{Base64 => ApacheBase64}

import java.util.regex.Pattern
import scala.util.{Failure, Success, Try}

object MediaType {
  def apply(maybeCompatibleMediaType: String): MediaType =
    Try {
      val extractMediaType = maybeCompatibleMediaType.split(":")(1).split(";")(0)
      extractMediaType match {
        case x if x.startsWith("image/")        => Image
        case x if x.contains("application/pdf") => Pdf
        case _                                  => Unknown
      }
    }.getOrElse(Unknown)
}

object StringUtils extends Loggie {
  def imagePdfContent(maybeImagePdfContent: Option[String]): String =
    maybeImagePdfContent.flatMap { imagePdf => Try(imagePdf.split(",")(1)).toOption }.getOrElse("")

  def mediaTypeCompatible(maybeImagePdf: Option[String]): Option[MediaType] =
    Try {
      maybeImagePdf.map { imgUrl =>
        if (isEmpty(imgUrl)) {
          Unknown
        } else {
          val parts = imgUrl.split(",")

          def sourceCheck(s: String): Boolean = {
            val pattern = "data:[A-Za-z0-9]+/[A-Za-z0-9]+;[A-Za-z]+64"
            val r = Pattern.compile(pattern)
            r.matcher(s).find()
          }

          val mediaType = MediaType(parts(0))
          sourceCheck(parts(0)) && mediaType.compatible && isBase64(Some(parts(1))) match {
            case true  => mediaType
            case false => Unknown
          }
        }
      }
    } match {
      case Failure(t) =>
        logger.error(t.getMessage, t)
        None
      case Success(value) => value
    }

  def isEmpty(cs: CharSequence): Boolean = cs == null || cs.length == 0

  def isBase64(maybeBase64: Option[String]): Boolean = {
    maybeBase64.fold(false)(base64 => {
      isEmpty(base64) match {
        case true => false
        case false =>
          val pattern =
            "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$"
          val r = Pattern.compile(pattern)
          r.matcher(base64).find()
      }
    })
  }

  def fromBase64(token: String): String =
    new String(
      ApacheBase64.decodeBase64(token)
    )

}
