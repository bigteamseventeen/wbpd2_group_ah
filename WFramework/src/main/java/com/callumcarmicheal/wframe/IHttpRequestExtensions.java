package com.callumcarmicheal.wframe;

public interface IHttpRequestExtensions {

    /**
     * Checks if the Message Page is implemented if not HttpRequest.SendMessageText is used.
     * @return
     */
    public boolean isSendMessagePageSupported();

    /**
     * Render a simple message page
     * @param request The http request
     * @param Title The page title
     * @param Message The page message content 
     * @param HttpResponse The response code 
     */
    public void SendMessagePage(HttpRequest request, String Title, String Message, int HttpResponse);


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
     * @param publicMessage The message that can be safely shown the end the user
     * @param debugMessage A message that is intended for developers (if the project is debugging)
     * @param exception The exception that has been thrown
     * @param escapePublicMessage If we are escaping the string (if false we have html)
     * @param escapeDebugMessage If we are escaping the string (if false we have html)
     */
    public void ThrowExceptionPage(String publicMessage, String debugMessage, Exception exception, boolean escapePublicMessage, boolean escapeDebugMessage);

}