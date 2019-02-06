package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.models;

import com.mitchellbosecke.pebble.extension.escaper.SafeString;
import org.unbescape.html.HtmlEscape;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    public int Index;
    public SafeString HtmlName;
    public SafeString HtmlDescription;
    public boolean InTitle;
    public boolean InDescription;
    public uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic Topic;

    public SearchResult(boolean title, boolean desc, Topic t, int i, String query) {
        this.InTitle = title;
        this.InDescription = desc;
        this.Topic = t;
        this.Index = i;

        generateHtml(query);
    }

    private void generateHtml(String q) {
        int qs = q.length();

        if (InTitle) {
            // Split the name
            Integer[] indexes = findIndex(q, Topic.getTitle());
            StringBuilder sb = new StringBuilder();
            String title = Topic.getTitle();

            highlightText(q, qs, indexes, sb, title);
            HtmlName = new SafeString("<a href=\"/topic?id=" + Index + "\">" + sb.toString() + "</a>");
        } else {
            HtmlName = new SafeString("<a href=\"/topic?id=" + Index + "\">" + Topic.getTitle() + "</a>");
        }

        if (InDescription) {
            // Split the name
            Integer[] indexes = findIndex(q, Topic.getDescription());
            StringBuilder sb = new StringBuilder();
            String description = Topic.getDescription();

            highlightText(q, qs, indexes, sb, description);
            HtmlDescription = new SafeString(sb.toString());
        } else {
            HtmlDescription = new SafeString(Topic.getDescription());
        }
    }

    private void highlightText(String q, int qs, Integer[] indexes, StringBuilder sb, String string) {
        int ts = string.length();
        int start = 0;

        for (Integer x : indexes) {
            sb.append(string, start, x);
            sb.append("<b>").append(HtmlEscape.escapeHtml5(q)).append("</b>");
            start += x + qs;
        }

        sb.append(string, start, ts);
    }

    private Integer[] findIndex(String needle, String haystack) {
        List<Integer> li = new ArrayList<Integer>();

        int index = haystack.indexOf(needle);
        while (index >= 0) {
            System.out.println(index);
            li.add(index);
            index = haystack.indexOf(needle, index + 1);
        }

        return li.toArray(new Integer[li.size()]);
    }
}
