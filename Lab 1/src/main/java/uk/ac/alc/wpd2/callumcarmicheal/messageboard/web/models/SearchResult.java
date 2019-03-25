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
        System.out.println("Q Size = " + qs);
        
        if (InTitle) {
            // Split the name
            Integer[] indexes = findIndex(q, Topic.getTitle());
            StringBuilder sb = new StringBuilder();
            String title = Topic.getTitle();
            
            System.out.println("Q Size Before = " + qs);
            highlightText(q, qs, indexes, sb, title);
            System.out.println("Q Size After = " + qs);
    
            HtmlName = new SafeString("<a href=\"/topic?id=" + Index + "\">" + sb.toString() + "</a>");
        } else {
            HtmlName = new SafeString("<a href=\"/topic?id=" + Index + "\">" + Topic.getTitle() + "</a>");
        }

        if (InDescription) {
            System.out.println("Q Size = " + qs);

            // Split the name
            Integer[] indexes = findIndex(q, Topic.getDescription());
            StringBuilder sb = new StringBuilder();
            String description = Topic.getDescription();

            System.out.println("Q Size Before = " + qs);
            highlightText(q, qs, indexes, sb, description);
            System.out.println("Q Size After = " + qs);
            
            HtmlDescription = new SafeString(sb.toString());
        } else {
            HtmlDescription = new SafeString(Topic.getDescription());
        }
    }

    private void highlightText(String q, int qs, Integer[] indexes, StringBuilder sb, String string) {
        int ts = string.length();
        int start = 0;

        for (Integer x : indexes) {
            // TODO: Replace highlightText(q...) with an algorithm to extract text from the original title
    
            sb.append(string, start, x)
                .append("<b>")
                    .append(HtmlEscape.escapeHtml5(string.substring(x, qs)))
                .append("</b>");
            
            start += x + qs;
        }

        sb.append(string, start, ts);
    }
    
    private Integer[] findIndex(String needle, String haystack) {
        return findIndex(needle, haystack, true);
    }
    
    private Integer[] findIndex(String needle, String haystack, boolean ignoreCase) {
        List<Integer> li = new ArrayList<Integer>();

        if (ignoreCase) {
            needle = needle.toLowerCase();
            haystack = haystack.toLowerCase();
        }
        
        int index = haystack.indexOf(needle);
        while (index >= 0) {
            li.add(index);
            index = haystack.indexOf(needle, index + 1);
        }

        return li.toArray(new Integer[0]);
    }
}
