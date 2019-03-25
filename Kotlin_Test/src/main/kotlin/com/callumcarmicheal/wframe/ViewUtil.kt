package com.callumcarmicheal.wframe

import com.google.common.primitives.Ints
import org.unbescape.html.HtmlEscape

class ViewUtil {
    fun E(e: String): String {
        return HtmlEscape.escapeHtml5(e)
    }

    fun UE(e: String): String {
        return HtmlEscape.unescapeHtml(e)
    }

    fun I(l: Long): Int {
        return Ints.checkedCast(l)
    }

    fun GenerateColorFromString(x: String): String {
        val i = x.hashCode()
        val c = Integer.toHexString(i and 0x00FFFFFF)
            .toUpperCase()

        return "00000".substring(0, 6 - c.length) + c
    }
}