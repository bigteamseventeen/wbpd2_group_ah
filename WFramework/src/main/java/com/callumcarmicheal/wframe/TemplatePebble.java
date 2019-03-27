package com.callumcarmicheal.wframe;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class TemplatePebble {
    public static PebbleEngine Engine = createEngine();
    public static String TEMPLATES_PATH = "templates/";
    private static ViewUtil vUtil = new ViewUtil();

    private static PebbleEngine createEngine() {
        PebbleEngine.Builder builder = new PebbleEngine.Builder();

        boolean isDebugging = Server.IsDebugging();
        
        return builder
            .cacheActive(false)
            //.cacheActive(! isDebugging)
            .build();
    }
    
    public static Context CreateContext() {
        Context ctx = new Context();

        // Place default settings here.
        ctx.put("U", vUtil);

        return ctx;
    }

    /**
     * Checks if a template exists
     * @param TemplateFile The template to lookup
     * @param TemplateIsFile States if the template is to be looked for in the working directory or as a resource
     */
    public static boolean TemplateExists(String TemplateFile, boolean TemplateIsFile) {
        // TODO: Handle resource management!

        if (TemplateIsFile) {
            File f = new File(TemplateFile);
            return f.exists();
        }

        else {
            System.err.println("UNIMPLEMENTED FUNCTION: c.cc.wframe.Template.TemplateExists(..., true) - Check if resource is valid. Defaulting to false");
            return false;
        }
    }

    public static com.mitchellbosecke.pebble.template.PebbleTemplate GetTemplate(String template, boolean TemplateIsFile) {
        return Engine.getTemplate(template);
    }

    public static String Execute(String Template, Context Ctx) throws IOException {
        return Execute(Template, Ctx, true);
    }

    public static String Execute(String Template, Context Ctx, boolean TemplateIsFile) throws IOException {
        com.mitchellbosecke.pebble.template.PebbleTemplate compiledTemplate = GetTemplate(Template, TemplateIsFile);
        Writer writer = new StringWriter();

        compiledTemplate.evaluate(writer, Ctx);
        return writer.toString();
    }
}
