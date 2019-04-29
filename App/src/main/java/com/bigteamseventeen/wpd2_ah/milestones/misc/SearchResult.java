package com.bigteamseventeen.wpd2_ah.milestones.misc;

import org.unbescape.html.HtmlEscape;

import java.util.ArrayList;
import java.util.List;

import com.bigteamseventeen.wpd2_ah.milestones.models.*;

public class SearchResult {
    public int Index;
    public String HtmlName;
    public String HtmlDescription;
    public boolean InTitle;
    public boolean InDescription;
    public Planner Planner;

    public SearchResult(boolean title, boolean desc, Planner planner, int idx, String query) {
        this.InTitle = title;
        this.InDescription = desc;
        this.Planner = planner;
        this.Index = idx;

        generateHtml(query);
    }

    private void generateHtml(String query) {
        int querySize = query.length();
        System.out.println("Q Size = " + querySize);
        
        if (InTitle) {
            // Split the name
            Integer[] indexes = findIndex(query, Planner.getTitle());
            StringBuilder sb = new StringBuilder();
            String title = Planner.getTitle();
            
            System.out.println("Q Size Before = " + querySize);
            highlightText(query, querySize, indexes, sb, title);
            System.out.println("Q Size After = " + querySize);
    
            HtmlName = ("<a href=\"/planner/view?id=" + Index + "\">" + sb.toString() + "</a>");
        } else {
            HtmlName = ("<a href=\"/planner/view?id=" + Index + "\">" + Planner.getTitle() + "</a>");
        }

        if (InDescription) {
            System.out.println("Q Size = " + querySize);

            // Split the name
            Integer[] indexes = findIndex(query, Planner.getDescription());
            StringBuilder sb = new StringBuilder();
            String description = Planner.getDescription();

            System.out.println("Q Size Before = " + querySize);
            highlightText(query, querySize, indexes, sb, description);
            System.out.println("Q Size After = " + querySize);
            
            HtmlDescription = (sb.toString());
        } else {
            HtmlDescription = (Planner.getDescription());
        }
    }

    private void highlightText(String query, int querySize, Integer[] indexes, StringBuilder sb, String originalString) {
        int originalSize = originalString.length();
        int start = 0;

        // Loop the indexes
        for (Integer idx : indexes) {
            sb.append(originalString, start, idx);
            sb.append("<b>");
            sb.append(HtmlEscape.escapeHtml5(originalString.substring(idx, idx+querySize)));
            sb.append("</b>");
            
            start += idx + querySize;
        }

        // Add the rest of the string
        sb.append(originalString, start, originalSize); 
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