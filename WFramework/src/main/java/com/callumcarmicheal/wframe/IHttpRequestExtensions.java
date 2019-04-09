package com.callumcarmicheal.wframe;

import java.io.IOException;

public interface IHttpRequestExtensions {

    /**
     * Checks if the Message Page is implemented if not HttpRequest.SendMessageText is used.
     * @return
     */
    public boolean isSendMessagePageSupported();

    /**
     * Render a simple message page
     * @param request           The http request
     * @param title             The page title
     * @param message           The page message content 
     * @param httpResponseCode  The response code 
     */
    public void SendMessagePage(HttpRequest request, String title, String message, int httpResponseCode) throws IOException;
    
    /**
     * Checks if the Exception Page is implemented if not HttpRequest.ThrowExceptionText is used.
     * @return
     */
    public boolean isThrowExceptionPageSupported();


    /**
     * Show a exception page
     * @param exception The exception that has been thrown
     */
    public void ThrowExceptionPage(Exception exception);

    /**
     * Show a exception page
     * @param publicMessage         The message that can be safely shown the end the user
     * @param debugMessage          A message that is intended for developers (if the project is debugging)
     * @param exception             The exception that has been thrown
     */
	public void ThrowExceptionPage(String publicMessage, String debugMessage, Exception exception);

    /**
     * Show a exception page
     * @param publicMessage         The message that can be safely shown the end the user
     * @param debugMessage          A message that is intended for developers (if the project is debugging)
     * @param exception             The exception that has been thrown
     * @param escapePublicMessage   If we are escaping the string (if false we have html)
     * @param escapeDebugMessage    If we are escaping the string (if false we have html)
     * @param httpResponseCode      The http response code returned in the header
     */
    public void ThrowExceptionPage(String publicMessage, String debugMessage, Exception exception, boolean escapePublicMessage, boolean escapeDebugMessage, int httpResponseCode);
}