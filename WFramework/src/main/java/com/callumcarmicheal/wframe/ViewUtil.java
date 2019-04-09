package com.callumcarmicheal.wframe;

import com.google.common.primitives.Ints;
import org.unbescape.html.HtmlEscape;

public class ViewUtil {

    /**
     * Escape string to html safe
     * @param str String to escape
     * @return
     */
    public String ESC(String str) {
        return HtmlEscape.escapeHtml5(str);
    }

    /**
     * Unescape html string
     * @param str String to unescape
     * @return
     */
    public String UESC(String str) { return HtmlEscape.unescapeHtml(str); }
    /**
     * Convert long to int
     * @param l
     * @return
     */
    public int L2I(Long l) { return Ints.checkedCast(l); }
    
    /**
     * Generate a color from a string
     * @param str
     * @return A hex color generated using string's hashcode
     */
    public String GenColFromStr(String str) {
        int i = str.hashCode();
        String c = Integer.toHexString(i & 0x00FFFFFF)
                .toUpperCase();

        return "00000".substring(0, 6 - c.length()) + c;
    }
}