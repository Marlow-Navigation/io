package com.marlow.io.utils

import org.apache.commons.codec.binary.{Base64 => ApacheBase64}

import java.util.regex.Pattern
import scala.util.Try

object StringUtils {
  def imageUrl(maybeImageUrl: Option[String]): String =
    maybeImageUrl.flatMap { imgUrl => Try(imgUrl.split(",")(1)).toOption }.getOrElse("")

  def isImageUrl(maybeImageUrl: Option[String]): Boolean = {
    maybeImageUrl.fold(false)(imgUrl => {
      isEmpty(imgUrl) match {
        case true => false
        case false =>
          Try {
            val parts = imgUrl.split(",")

            def sourceCheck(s: String): Boolean =
              Try {
                val pattern = "data:[A-Za-z0-9]+/[A-Za-z0-9]+;[A-Za-z]+64"
                val r = Pattern.compile(pattern)
                r.matcher(s).find()
              }.getOrElse(false)

            def isImageMediaTypeCheck(s: String): Boolean =
              Try {
                s.split(":")(1).split(";")(0).startsWith("image/")
              }.getOrElse(false)

            sourceCheck(parts(0)) && isImageMediaTypeCheck(parts(0)) && isBase64(Some(parts(1)))

          }.getOrElse(false)
      }
    })
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
