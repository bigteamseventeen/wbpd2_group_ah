package com.bigteamseventeen.wpd2_ah.milestones;

import java.io.IOException;

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
            new Renderer()
                .render(request, "_framework/message", httpResponseCode, ImmutableMap.<String, Object>builder()
                    .put("messageTitle", title)
                    .put("messageText", message)
                    .build());
        } catch (IOException e) { request.throwException(e); }
    }

    @Override public boolean isThrowExceptionPageSupported() { return false; }
    @Override public void      ThrowExceptionPage(HttpRequest request, Exception exception) { 
        ThrowExceptionPage(request, "I'm sorry there was an error processing you're request.", 
            exception.getMessage(), exception, true, true, 500); }
    @Override public void      ThrowExceptionPage(HttpRequest request, String publicMessage, String debugMessage, Exception exception) {
        ThrowExceptionPage(request, publicMessage, debugMessage, exception, true, true, 500); }
    @Override public void      ThrowExceptionPage(HttpRequest request, String publicMessage, String debugMessage, Exception exception, boolean escapePublicMessage, boolean escapeDebugMessage, int httpResponseCode) { 
        try {
            new Renderer()
                .render(request, "_framework/message", httpResponseCode, ImmutableMap.<String, Object>builder()
                    .put("messageTitle", "An error occured")
                    .put("messageText", publicMessage)
                    .put("debugMessage", debugMessage)
                    .put("exception", exception)
                    .build());
        } catch (IOException e) { 
            logger.error("IOException during printing framework message: ", e);
            request.throwExceptionText(publicMessage, debugMessage, exception, httpResponseCode);
        }
    }
}