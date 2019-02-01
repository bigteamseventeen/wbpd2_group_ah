package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web;

import com.google.common.primitives.Ints;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class ViewUtil {

    public String Escape(String e) {
        return escapeHtml4(e);
    }
    public int I(Long l) {return Ints.checkedCast(l);}
}
