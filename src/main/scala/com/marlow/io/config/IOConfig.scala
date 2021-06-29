package com.marlow.io.config

case class IOConfig(
    defaultPageSize: String,
    defaultOrientation: String,
    defaultPageNumbers: Boolean,
    defaultPageNumbersFontsize: Int,
    defaultFont: String,
    defaultFontBold: String,
    defaultFontsize: Int,
    defaultAlignment: String,
    defaultMarginTop: Int,
    defaultMarginRight: Int,
    defaultMarginBottom: Int,
    defaultMarginLeft: Int,
    keyPageSize: String,
    keyFont: String,
    keyFontBold: String,
    keyFontSize: String,
    keyAlignment: String,
    keyText: String,
    keyHeader: String,
    keyFooter: String,
    keyTables: String,
    keyTable: String,
    keyHtml: String,
    tableHeaderBgColor: Float,
    tableRowBgColor: Float
)
