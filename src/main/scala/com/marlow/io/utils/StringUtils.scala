package com.marlow.io.utils

import org.apache.commons.codec.binary.{Base64 => ApacheBase64}

import java.util.regex.Pattern

object StringUtils {
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
