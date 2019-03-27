package com.callumcarmicheal.wframe;

import com.callumcarmicheal.wframe.TemplatePebble;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mitchellbosecke.pebble.error.LoaderException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import org.unbescape.html.HtmlEscape;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpRequest {
	/** ---- Settings ---- */
	public static IHttpRequestExtensions HttpExtensions = null;
	/** ---- Settings ---- */

	private boolean sentResponse = false;
	private ByteArrayDataOutput buffer;
	public HttpExchange Exchange;
	public HttpRequest() {}
	
	public HttpRequest(HttpExchange e) {
		Prepare(e);
	}
	
	protected void Prepare(HttpExchange e) {
		this.Exchange = e;
		this.buffer = ByteStreams.newDataOutput();
	}

	public boolean hasSentResponse() {
		return sentResponse;
	}
	
	// Write data into the buffer
	public void Write(String str)   { buffer.write(str.getBytes()); }
	public void Write(byte[] bytes) { buffer.write(bytes); }
	public void Write(int i)        { buffer.write(i); }
	
	// Redirect to a page
	public void Redirect(String to) throws IOException { Redirect(to, "Redirecting to " + to); }
	public void Redirect(String to, String why) throws IOException {
		Headers headers = Exchange.getResponseHeaders();
		headers.add("Location", to);
		Send(302, why);
	}
	
	/**
	 * Send the http response code with buffer
	 * @param Response
	 * @throws IOException
	 */
	public void Send(int Response) throws IOException {
		byte[] buf = buffer.toByteArray();
		Send(Response, buf);
	}
	
	public void Send(int Response, byte[] Buffer) throws IOException {
		Exchange.sendResponseHeaders(Response, Buffer.length);
		OutputStream os = Exchange.getResponseBody();
		os.write(Buffer);
		os.close();

		sentResponse = true;
	}
	
	public void Send(byte[] Buffer) throws IOException {
		Send(200, Buffer);
	}
	
	public void Send(String data) throws IOException {
		Write(data);
		Send(200);
	}
	
	public void Send(int Response, String data) throws IOException {
		Write(data);
		Send(Response);
	}
	
	public void Send() throws IOException {
		Send(200);
	}
	
	public void ThrowExceptionText(String Message, Exception e) {
		ThrowExceptionText(Message, Message, e);
	}

	public void ThrowExceptionText(String PublicMessage, String DebugMessage, Exception ex) {
		ThrowExceptionText(PublicMessage, DebugMessage, ex, true, true);
	}

	public void ThrowExceptionText(String PublicMessage, String DebugMessage, Exception ex, boolean EscapePublicMessage, boolean EscapeDebugMessage) {
		if (EscapePublicMessage)
			PublicMessage = HtmlEscape.escapeHtml5(PublicMessage);

		if (EscapeDebugMessage)
			DebugMessage = HtmlEscape.escapeHtml5(DebugMessage);

		if (Server.IsDebugging()) {
			Write("<h1>There was an error</h1><p>Im sorry there was a error loading resources.</p>");
			Write("<br><p>Exception message : <b>" + ex.getMessage() + "</b></p>");
			Write("<br><pre style=\"background:#ccc\">");
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			Write(sw.toString());
			
			Write("</pre>");

			try { Send(500); } catch (Exception e) { }
		} else {
			try {
				Send(500, "<h1>There was an error</h1><p>Im sorry there was a error loading resources.</p>");
			} catch (Exception e) { }
		}
	}

	public void ThrowException(Exception ex) {
		ex.printStackTrace();
		
		// If the exception is an LoaderException (PEBBLE SUPPORT)
		// assume that we could not load a file and assume its the exception template
		if (ex instanceof LoaderException) {
			// Clear the output buffer.
			buffer = ByteStreams.newDataOutput();
			
			// Attempt to tell the browser something went wrong.
			ThrowExceptionText("Im sorry there was a error loading resources.", ex);
		}
		
		else {
			System.out.println(ex.getMessage());

			if (HttpExtensions == null) {

			}
		}
	}
	
	
	public Map<String,String> getQuery() {
		return Server.ParseQuery(this.Exchange);
	}
	
	public String getQueryString() {
		return Server.GetQueryString(this.Exchange);
	}
	
	public byte[] GetPost() throws IOException {
		Headers requestHeaders = Exchange.getRequestHeaders();
		Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();
		
		int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));
		
		InputStream is = Exchange.getRequestBody();
		byte[] data = new byte[contentLength];
		is.read(data);
		
		return data;
	}
	
	public Map<String,String> GetPostForm() throws IOException {
		return Server.ParseQueryEncoding(new String(GetPost()));
	}
	
	public void Clear() {
		this.buffer = ByteStreams.newDataOutput();
	}
	
	public void SendMessagePage(String Title, String Message) {
		SendMessagePage(Title, Message, 200);
	}
	
	public void SendMessagePage(String Title, String Message, int HttpResponse) {
		Clear();

		if (HttpExtensions == null) {
			SendMessageText(Title, Message, HttpResponse);
			return;
		}
		
		// Invoke the message page
		HttpExtensions.SendMessagePage(this, Title, Message, HttpResponse);

		// if (SET_Template_Generic_Message_Template == null) {
		// 	System.err.println("SendMessagePage: Template is not specified, defaulting to text (SET_Template_Generic_Message_Template)");
		// 	SendMessageText(Title, Message, HttpResponse);
		// 	return;
		// }

		// if (!TemplatePebble.TemplateExists(SET_Template_Generic_Message_Template, SET_Template_Generic_Message_Template_isResource)) {
		// 	System.err.println("SendMessagePage: Template cant be found (" + SET_Template_Generic_Message_Template + 
		// 		"), isRes = " + SET_Template_Generic_Message_Template_isResource);
		// 	SendMessageText(Title, Message, HttpResponse);
		// 	return;
		// }

		// Context ctx = TemplatePebble.CreateContext();
		// ctx.put("MessageTitle", Title);
		// ctx.put("MessageText", Message);

		// try {
		// 	Send(HttpResponse, TemplatePebble.Execute(SET_Template_Generic_Message_Template, ctx, 
		// 		SET_Template_Generic_Message_Template_isResource));
		// } catch (Exception e) { ThrowException(e); }
	}

	public void SendMessageText(String Title, String Message) {
		SendMessageText(Title, Message, 200);
	}

	public void SendMessageText(String Title, String Message, int HttpResponse) {
		Title = HtmlEscape.escapeHtml5(Title);
		Message = HtmlEscape.escapeHtml5(Message);
		
		try { Send(HttpResponse, "<h1>"+Title+"</h1><p>"+Message+"</p>"); } 
		catch (Exception e) { }
	}
	
	public boolean SendFile_s(int code, File f) {
		try {
			SendFile(code, f);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public void SendFile(int code, File f) throws IOException {
		// Clear the output buffer as we dont want to use it
		Clear();
		
		ByteBuffer buffer;
		String mime_type;
		
		try {
			mime_type = Files.probeContentType(f.toPath());
		} catch (Exception ex) {
			SendMessagePage(
					"Resource not found",
					"The requested resource could not be found.", 404);
			return;
		}
		
		try (FileInputStream stream = new FileInputStream(f)) {
			FileChannel inChannel = stream.getChannel();
			
			buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
			buffer.order( ByteOrder.BIG_ENDIAN );
		} catch (Exception ex) {
			SendMessagePage(
					"Resource not found",
					"The requested resource could not be found.", 404);
			return;
		}
		
		Files.probeContentType(f.toPath());
		
		Headers headers = Exchange.getResponseHeaders();
		headers.add("Content-Type", mime_type);
		
		if (buffer.hasArray()) {
			Send(buffer.array());
			return;
		}
		
		byte[] arr = new byte[buffer.remaining()];
		buffer.get(arr);
		Send(arr);
	}
	
	String _request = null;
	String _request_query = null;
	public String getRequestURI(boolean removeQuery) {
		if (removeQuery) {
			if (_request != null)
				return _request;
				
			_request = Exchange.getRequestURI().toString();
			return _request.contains("?") ? _request = _request.split("\\?")[0] : _request;
		}
		
		if (_request_query != null)
			return _request_query;
			
		return _request_query = Exchange.getRequestURI().toString();
	}
}