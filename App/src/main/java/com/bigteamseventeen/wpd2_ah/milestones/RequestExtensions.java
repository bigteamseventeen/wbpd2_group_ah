package com.bigteamseventeen.wpd2_ah.milestones;

import java.io.IOException;

import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.IHttpRequestExtensions;
import com.google.common.collect.ImmutableMap;

public class RequestExtensions implements IHttpRequestExtensions {

    @Override public boolean isSendMessagePageSupported() { return false;  }
    @Override public void      SendMessagePage(HttpRequest request, String title, String message, int httpResponseCode) {
        // Render the message page
        try {
            new Renderer()
                .render(request, "_framework/message", httpResponseCode, ImmutableMap.<String, Object>builder()
                    .put("messageTitle", title)
                    .put("messageText", message)
                    .build());
        } catch (IOException e) { request.ThrowException(e); }
    }

    @Override public boolean isThrowExceptionPageSupported() { return false; }
    @Override public void      ThrowExceptionPage(Exception exception) { }
    @Override public void      ThrowExceptionPage(String publicMessage, String debugMessage, Exception exception) {}
    @Override public void      ThrowExceptionPage(String publicMessage, String debugMessage, Exception exception, boolean escapePublicMessage, boolean escapeDebugMessage, int httpResponseCode) {}
}