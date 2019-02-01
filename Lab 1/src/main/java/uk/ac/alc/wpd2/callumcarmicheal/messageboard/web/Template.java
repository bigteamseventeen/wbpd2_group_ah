package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic;

import java.io.StringWriter;
import java.io.Writer;

public class Template {
    public static PebbleEngine Engine = createEngine();
    private static ViewUtil vUtil = new ViewUtil();

    private static PebbleEngine createEngine() {
        PebbleEngine.Builder builder = new PebbleEngine.Builder();

        return builder
                .cacheActive(! Server.IsDebugging())
                .build();
    }

    public static Context CreateContext() {
        Context ctx = new Context();

        // Place default settings here.
        ctx.put("Board", WebBoard.MB);
        ctx.put("U", vUtil);

        return ctx;
    }

    public static PebbleTemplate GetTemplate(String template) {
        return Engine.getTemplate("templates/" + template + ".peb");
    }

    public static String Execute(String template, Context context) throws Exception {
        PebbleTemplate compiledTemplate = GetTemplate(template);
        Writer writer = new StringWriter();

        compiledTemplate.evaluate(writer, context);
        return writer.toString();
    }
}
