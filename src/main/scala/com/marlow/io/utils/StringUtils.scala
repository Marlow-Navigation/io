package com.marlow.io.utils

object StringUtils {
  val EmptyString = ""

  def isEmpty(cs: CharSequence): Boolean = cs == null || cs.length == 0

  def asOpt(cs: CharSequence): Option[String] = {
    val stringNoWhiteSpace: CharSequence = deleteWhitespace(cs.toString)
    isEmpty(stringNoWhiteSpace) match {
      case true  => None
      case false => Some(cs.toString)
    }
  }

  def deleteWhitespace(str: String): String = {
    isEmpty(str) match {
      case true  => EmptyString
      case false => str.replaceAll("\\s", "")
    }
  }

  def trim(str: String): String = {
    isEmpty(str) match {
      case true  => EmptyString
      case false => str.trim
    }
  }

}
