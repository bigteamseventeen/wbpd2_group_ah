package com.bigteamseventeen.wpd2_ah.milestones;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.IHttpRequestExtensions;
import com.google.common.collect.ImmutableMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestExtensions implements IHttpRequestExtensions {
    final static Logger logger = LogManager.getLogger();
    
    @Override public boolean isSendMessagePageSupported() { return true;  }
    @Override public void      SendMessagePage(HttpRequest request, String title, String message, int httpResponseCode) {
        // Render the message page
        try {
            new Renderer().render(request, "_framework/message", httpResponseCode, HashBuilder.<String, Object>builder()
                .put("messageTitle", title)
                .put("messageText", message)
                .build());
        } catch (IOException e) { request.throwException(e); }
    }

    @Override public boolean isThrowExceptionPageSupported() { return true; }
    @Override public void      ThrowExceptionPage(HttpRequest request, Exception exception) { 
        ThrowExceptionPage(request, "I'm sorry there was an error processing you're request.", exception.getMessage(), exception, true, true, 500); }
    @Override public void      ThrowExceptionPage(HttpRequest request, String publicMessage, String debugMessage, Exception exception) {
        ThrowExceptionPage(request, publicMessage, debugMessage, exception, true, true, 500); }
    @Override public void      ThrowExceptionPage(HttpRequest request, String publicMessage, String debugMessage, Exception exception, boolean escapePublicMessage, boolean escapeDebugMessage, int httpResponseCode) { 
        try {
            StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            
            new Renderer().render(request, "_framework/message", httpResponseCode, HashBuilder.<String, Object>builder()
                .put("messageTitle", "A error has occurred, Please try again later.")
                .put("messageText", publicMessage)
                .put("debugMessage", debugMessage)
                .put("exception", exception)
                .put("stacktrace", sw.toString())
                .build()); 
        } catch (IOException e) { 
            logger.error("IOException during printing framework message: ", e);
            request.throwExceptionText(publicMessage, debugMessage, exception, httpResponseCode);
        }
    }
}