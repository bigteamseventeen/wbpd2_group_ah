package com.callumcarmicheal.wframe;

import com.callumcarmicheal.wframe.web.Session;
import com.callumcarmicheal.wframe.web.SessionList;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unbescape.html.HtmlEscape;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;

public class HttpRequest {
    final static Logger logger = LogManager.getLogger();	

	/** ---- Settings ---- */
	public static IHttpRequestExtensions HttpExtensions = null;
	/** ---- Settings ---- */

	private boolean sentResponse = false;
	private ByteArrayDataOutput buffer;
	private Session sessionState = null;
	private SessionList sessionList = null;

	/** ---- Public Properties ---- */
	public HttpExchange Exchange;
	/** ---- Public Properties ---- */

	public HttpRequest(HttpExchange httpExchange, SessionList sessions) {
		this.sessionList = sessions;

		// Setup the httpExchange
		Prepare(httpExchange);

		// If we have a session
		String sessionId = null;
		if ((sessionId = this.getRequestCookie(sessions.COOKIE_HEADER)) != null) {
			logger.trace("HttpRequest: Found session id - " + sessionId);
			if (sessions.exists(sessionId)) {
				logger.trace("HttpRequest: §a Session exists setting session header");
				sessionState = sessions.get(sessionId);
				setSessionHeader(sessionState);
			} else { /**/ 
				logger.trace("HttpRequest: §c Session does not exist!");				
			}
		} else { /**/
			logger.trace("HttpRequest: §c Session cookie not found!");				
		}
	}

	protected void Prepare(HttpExchange httpExchange) {
		this.Exchange = httpExchange;
		this.buffer = ByteStreams.newDataOutput();
	}

	public boolean hasSentResponse() {
		return sentResponse;
	}

	// Write data into the buffer

	/**
	 * Append string to the output buffer
	 * 
	 * @param str
	 */
	public void Write(String str) {
		buffer.write(str.getBytes());
	}

	/**
	 * Append bytes to output buffer
	 * 
	 * @param bytes
	 */
	public void Write(byte[] bytes) {
		buffer.write(bytes);
	}

	/**
	 * Append integer to output buffer
	 * 
	 * @param i
	 */
	public void Write(int i) {
		buffer.write(i);
	}

	/**
	 * Redirect to another page
	 * 
	 * @param to The url to be redirected to
	 * @throws IOException
	 */
	public void Redirect(String to) throws IOException {
		Redirect(to, "Redirecting to " + to);
	}

	/**
	 * Redirect to another page
	 * 
	 * @param to  The url to be redirected to
	 * @param why Why the user is being redirected
	 * @throws IOException
	 */
	public void Redirect(String to, String why) throws IOException {
		Headers headers = Exchange.getResponseHeaders();
		headers.add("Location", to);
		Send(302, why);
	}

	/**
	 * Send the http response code with existing buffer
	 * 
	 * @param response
	 * @throws IOException
	 */
	public void Send(int response) throws IOException {
		byte[] buf = buffer.toByteArray();
		Send(response, buf);
	}

	/**
	 * Send the http response with specified bytes buffer
	 * 
	 * @param response The http response code
	 * @param buffer   Byte array to be sent to client
	 * @throws IOException
	 */
	public void Send(int response, byte[] buffer) throws IOException {
		Exchange.sendResponseHeaders(response, buffer.length);
		OutputStream os = Exchange.getResponseBody();
		os.write(buffer);
		os.close();

		sentResponse = true;
	}

	/**
	 * Send current buffer with appended data and send OK (200)
	 * 
	 * @param buffer Data to be appended to the buffered output
	 * @throws IOException
	 */
	public void Send(byte[] buffer) throws IOException {
		Send(200, buffer);
	}

	/**
	 * Send current buffer with appended data and send OK (200)
	 * 
	 * @param data Data to be appended to the buffered output
	 * @throws IOException
	 */
	public void Send(String data) throws IOException {
		Write(data);
		Send(200);
	}

	/**
	 * Send current buffer with appended data and Response
	 * 
	 * @param response Http response code
	 * @param data     Data to be appended to the buffered output
	 * @throws IOException
	 */
	public void Send(int response, String data) throws IOException {
		Write(data);
		Send(response);
	}

	/**
	 * Send current buffer with 200 (OK) http header
	 * 
	 * @throws IOException
	 */
	public void Send() throws IOException {
		Send(200);
	}

	/**
	 * Send a exception page to the client with added user safe information
	 * 
	 * @param message   The message that is displayed to the end user and developer
	 * @param exception The exception
	 */
	public void throwExceptionText(String message, Exception exception) {
		throwExceptionText(message, message, exception);
	}

	/**
	 * Send a exception page to the client with added user safe information
	 * 
	 * @param message          The message that is displayed to the end user and
	 *                         developer
	 * @param exception        The exception
	 * @param httpResponseCode The http error code sent in the header
	 */
	public void throwExceptionText(String message, Exception exception, int httpResponseCode) {
		throwExceptionText(message, message, exception, httpResponseCode);
	}

	/**
	 * Send a exception page to the client with added user safe information
	 * 
	 * @param publicMessage The message that is displayed to the end user
	 * @param debugMessage  The debug message that is visible when the application
	 *                      is being debugged
	 * @param exception     The exception
	 */
	public void throwExceptionText(String publicMessage, String debugMessage, Exception exception) {
		throwExceptionText(publicMessage, debugMessage, exception, true, true, 500);
	}

	/**
	 * Send a exception page to the client with added user safe information
	 * 
	 * @param publicMessage    The message that is displayed to the end user
	 * @param debugMessage     The debug message that is visible when the
	 *                         application is being debugged
	 * @param exception        The exception
	 * @param httpResponseCode The http error code sent in the header
	 */
	public void throwExceptionText(String publicMessage, String debugMessage, Exception exception,
			int httpResponseCode) {
		throwExceptionText(publicMessage, debugMessage, exception, true, true, httpResponseCode);
	}

	/**
	 * Send a exception page to the client with added user safe information
	 * 
	 * @param publicMessage       The message that is displayed to the end user
	 * @param debugMessage        The debug message that is visible when the
	 *                            application is being debugged
	 * @param exception           The exception
	 * @param escapePublicMessage If we are escaping the public message (set to
	 *                            false for HTML)
	 * @param escapeDebugMessage  If we are escaping the debug message (set to false
	 *                            for HTML)
	 * @param httpResponseCode    The http error code sent in the header
	 */
	public void throwExceptionText(String publicMessage, String debugMessage, Exception exception,
			boolean escapePublicMessage, boolean escapeDebugMessage, int httpResponseCode) {
		if (escapePublicMessage)
			publicMessage = HtmlEscape.escapeHtml5(publicMessage);

		if (escapeDebugMessage)
			debugMessage = HtmlEscape.escapeHtml5(debugMessage);

		if (Server.IsDebugging()) {
			Write("<h1>There was an error</h1><p>Im sorry there was a error loading resources.</p>");
			Write("<br><p>Exception message : <b>" + exception.getMessage() + "</b></p>");
			Write("<br><pre style=\"background:#ccc\">");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			Write(sw.toString());

			Write("</pre>");

			try {
				Send(httpResponseCode);
			} catch (Exception e) { }
		} else {
			try {
				Send(httpResponseCode,
						"<h1>There was an error</h1><p>Im sorry there was a error loading resources.</p>");
			} catch (Exception e) { }
		}
	}

	// TODO: add some throwException methods that allow status codes (500, 403 etc).

	/**
	 * Throw an exception (attempt to use a page or fallback to a basic page)
	 * 
	 * @param exception
	 */
	public void throwException(Exception exception) {
		// Print the error
		exception.printStackTrace();

		// If we can call the exception page then invoke it
		if (HttpExtensions != null && HttpExtensions.isThrowExceptionPageSupported()) {
			HttpExtensions.ThrowExceptionPage(this, exception);
			return;
		}

		// Fallback to a text based error page
		throwExceptionText("I'm sorry there was an error while processing your request.", exception);
	}

	
	/**
	 * Send a exception page to the client with added user safe information
	 * 
	 * @param publicMessage       The message that is displayed to the end user
	 * @param exception           The exception
	 */
	public void throwException(String publicMessage, Exception exception) {
		throwException(publicMessage, publicMessage, exception);
	}

	/**
	 * Send a exception page to the client with added user safe information
	 * 
	 * @param publicMessage       The message that is displayed to the end user
	 * @param debugMessage        The debug message that is visible when the
	 *                            application is being debugged
	 * @param exception           The exception
	 */
	public void throwException(String publicMessage, String debugMessage, Exception exception) {
		// If we can call the exception page then invoke it
		if (HttpExtensions != null && HttpExtensions.isThrowExceptionPageSupported()) {
			HttpExtensions.ThrowExceptionPage(this, publicMessage, debugMessage, exception);
			return;
		}

		// Forward to text page
		throwExceptionText(publicMessage, debugMessage, exception);
	}

	/**
	 * Get a string map of the query string
	 * 
	 * @return A map containing the queries
	 */
	public Map<String, String> getQuery() {
		return Server.ParseQuery(this.Exchange);
	}

	/**
	 * Get the query string from the URI
	 * 
	 * @return
	 */
	public String getQueryString() {
		return Server.GetQueryString(this.Exchange);
	}

	/**
	 * Get post body content as bytes
	 * 
	 * @return Byte array containg post body
	 * @throws IOException
	 */
	public byte[] GetPostBytes() throws IOException {
		Headers requestHeaders = Exchange.getRequestHeaders();
		int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));

		InputStream is = Exchange.getRequestBody();
		byte[] data = new byte[contentLength];
		is.read(data);

		return data;
	}

	/**
	 * Get the post form as a string map
	 * 
	 * @return
	 * @throws IOException
	 */
	public Map<String, String> GetPostForm() throws IOException {
		return Server.ParseQueryEncoding(new String(GetPostBytes()));
	}

	/**
	 * Clear the buffer and recreate the bytestream
	 */
	public void Clear() {
		this.buffer = ByteStreams.newDataOutput();
	}

	/**
	 * Generate a html message page
	 * 
	 * @param title
	 * @param message
	 */
	public void SendMessagePage(String title, String message) {
		SendMessagePage(title, message, 200);
	}

	/**
	 * Generate a html message page
	 * 
	 * @param title
	 * @param message
	 * @param httpResponseCode The http response code
	 */
	public void SendMessagePage(String title, String message, int httpResponseCode) {
		Clear();

		// Check if the SendMessagePage function is implemented
		if (HttpExtensions != null && HttpExtensions.isSendMessagePageSupported()) {
			try {
				HttpExtensions.SendMessagePage(this, title, message, httpResponseCode);
			} catch (IOException e) {
				throwException(e);
			} return;
		}
		
		// Invoke the message page
		SendMessageText(title, message, httpResponseCode);
	}

	/**
	 * Generate a basic html message page
	 * @param title
	 * @param message
	 */
	public void SendMessageText(String title, String message) {
		SendMessageText(title, message, 200);
	}

	/**
	 * Generate a basic html message page
	 * @param title
	 * @param message
	 * @param httpResponseCode
	 */
	public void SendMessageText(String title, String message, int httpResponseCode) {
		title = HtmlEscape.escapeHtml5(title);
		message = HtmlEscape.escapeHtml5(message);
		
		try { Send(httpResponseCode, "<h1>"+title+"</h1><p>"+message+"</p>"); } 
		catch (Exception e) { }
	}
	
	/**
	 * Write a file to the client
	 * <p>Without raising any exceptions through the chain</p>
	 * @param httpResponseCode 	Http Response Code
	 * @param file 				The file to be sent to the client
	 * @return If the file was successfully sent to the client
	 */
	public boolean SendFile_s(int httpResponseCode, File file) {
		try {
			SendFile(httpResponseCode, file);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Write a file to the client
	 * @param httpResponseCode	Http Response Code
	 * @param file 				The file to be sent to the client
	 * @throws IOException		The exception thrown when trying to read the file
	 */
	public void SendFile(int httpResponseCode, File file) throws IOException {
		// Clear the output buffer as we dont want to use it
		Clear();

		ByteBuffer buffer; // Local buffer
		String mime_type;  // Http mime type
		
		// Attempt to get the MIME_TYPE for the file
		try {
			// Attempt to find the mime type for the file
			mime_type = Files.probeContentType(file.toPath());

			// Check if the mime type cannot be determined
			if (mime_type == null) {
				// Use the default mime type stated by RFC
				// RFC 2046 - section 4.5.1: The "octet-stream" subtype is used to indicate that a body contains arbitrary binary data.
				mime_type = "application/octet-stream";
			}
		} catch (Exception ex) {
			// Send the error page
			SendMessagePage(
				"Resource not found",
				"The requested resource could not be found.", 404);
			return;
		}
		
		// Try to read the file in a stream
		try (FileInputStream stream = new FileInputStream(file)) {
			// Get the file channel
			FileChannel inChannel = stream.getChannel();
			
			// Read in the file into the output buffer
			buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());

			// Set the byte order to BIG ENDIAN (support for various byte orders)
			buffer.order( ByteOrder.BIG_ENDIAN ); 
		} catch (Exception ex) {
			// Send the error page
			SendMessagePage(
				"Resource not found",
				"The requested resource could not be found.", 404);
			return;
		}
		
		// Get the headers and set the content type
		Headers headers = Exchange.getResponseHeaders();
		headers.add("Content-Type", mime_type);
		
		// If we can convert to an array
		if (buffer.hasArray()) {
			// Send the array
			Send(buffer.array());
			return;
		}
		
		// Create a new array using the buffer
		byte[] arr = new byte[buffer.remaining()];
		buffer.get(arr);
		
		// Send the http response
		Send(arr); 
	}
	
	/** Cached uri without the query */
	String _request_uri = null;
	/** Cached uri including query */
	String _request_uri_query = null;

	/**
	 * Get the request URI
	 * @param removeQuery Strip the query from the URI
	 * @return The URI with or without the query stripped
	 */
	public String getRequestURI(boolean removeQuery) {
		// If we are removing the query from the uri
		if (removeQuery) {
			// Caching
			if (_request_uri != null)
				return _request_uri;
				
			// Get the URI and strip the query
			_request_uri = Exchange.getRequestURI().toString();
			return _request_uri.contains("?") ? _request_uri = _request_uri.split("\\?")[0] : _request_uri;
		}
		
		// Caching
		if (_request_uri_query != null)
			return _request_uri_query;
			
		// Return the request uri
		return _request_uri_query = Exchange.getRequestURI().toString();
	}

	/** Caching the request cookies */
	private Map<String,String> _cookies = null;
	private Map<String,String> _pubCookies = null;

	/**
	 * Get cookies from request
	 * @return
	 */
	public Map<String,String> getRequestCookies() {
		// Check if we have cached the cookies
		if (_cookies == null) {
			// Get the request cookies (can be a list of multiples)
			List<String> cookiesList = new ArrayList<>();
			List<String> headerCookies = Exchange.getRequestHeaders().get("Cookie");

			if (headerCookies != null) {
				// Loop request
				for (String s : headerCookies)
					// Split the cookies
					for (String c : s.split(";"))
						// Add the parsed cookie
						cookiesList.add(c.trim());

				// Set the cookies
				_cookies = parseCookies(cookiesList);
			}
			else {
				// We dont have any cookies so just set the cookie list to empty
				_cookies = new HashMap<>();
			}
		}

		// If we have not cached out cookies then cache them
		if (_pubCookies == null)
			_pubCookies = Collections.unmodifiableMap(_cookies);

		// Return the non modifably list
		return _pubCookies;
	}
	
	/**
	 * Get a cookie from the request
	 * @param cookieKey		THe cookie key
	 * @return				The value or null if not found
	 */
	public String getRequestCookie(String cookieKey) {
		// Have we cached the cookies
		if (_cookies == null)
			getRequestCookies(); // Parse cookies then get cookieKey
		return _cookies.get(cookieKey);
	}

	/**
	 * Create a map from a cookie header string
	 * @param listOfCookies The header string containing the cookies
	 * @return				 A map containing the cookies
	 */
	private static Map<String, String> parseCookies(List<String> listOfCookies) {
		// Cookies output
		Map<String, String> result = new LinkedHashMap<String, String>();
		
		// If we have cookies
		if (listOfCookies != null) {
			// Loop the cookies
			for (int i = 0; i < listOfCookies.size(); i++) {
				// Split by equals sign
				String[] parts = listOfCookies.get(i).split("=", 2);
				String value = parts.length > 1 ? parts[1] : "";

				// If we have more equals signs, escape them
				if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\""))
					value = value.substring(1, value.length() - 1);
				
				// Put the values into the cookies list
				result.put(parts[0], value);
			}
		}

		// Return the cookies
		return result;
	}

	private boolean _setSessionHeader = false;

	/**
	 * Get or create session key
	 * @return
	 */
	public Session session() {
		// If we have a session
		if (this.sessionState != null) {
			if (!_setSessionHeader)
				setSessionHeader(this.sessionState);
			return this.sessionState;
		}

		// Create a new one
		this.sessionState = this.sessionList.create();
		if (this.sessionState != null) 
			// Store the session in the response cookies
			setSessionHeader(this.sessionState);

		return this.sessionState;
	}

	private void setSessionHeader(Session session) {
		// Store the session in the response cookies
		Headers headers = Exchange.getResponseHeaders();

		// Remove existing Session
		if (headers.containsKey("Set-Cookie")) {
			List<String> l = headers.get("Set-Cookie");

			for (String s : l) 
				if (s.startsWith(sessionList.COOKIE_HEADER + "=")) 
					headers.remove(s);
		}

		headers.add("Set-Cookie", sessionList.COOKIE_HEADER + "=" + session.getSessionKey() + "; HttpOnly; Path=/; SameSite=Strict; Expires=" + sessionList.getExpirationSeconds());
		_setSessionHeader = true;
	}
}