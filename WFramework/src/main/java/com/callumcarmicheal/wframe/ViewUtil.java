package com.callumcarmicheal.wframe;

import com.google.common.primitives.Ints;
import org.unbescape.html.HtmlEscape;

public class ViewUtil {

    public String E(String e) {
        return HtmlEscape.escapeHtml5(e);
    }
    public String UE(String e) { return HtmlEscape.unescapeHtml(e); }
    public int I(Long l) { return Ints.checkedCast(l); }
    
    public String GenerateColorFromString(String x) {
        int i = x.hashCode();
        String c = Integer.toHexString(i & 0x00FFFFFF)
                .toUpperCase();

        return "00000".substring(0, 6 - c.length()) + c;
    }
}