package uk.ac.alc.wpd2.callumcarmicheal.messageboard;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TopicMessage {
	private String Author;
	private String Message;
	private String Date;
	
	public TopicMessage(String Author, String Message, String Date) {
		this.Author = Author;
		this.Message = Message;
		this.Date = Date;
	}
	
	public TopicMessage(String Author, String Message) {
		this(Author, Message, new SimpleDateFormat("dd/mm/yyyy").format(new Date()));
	}
	
// ---- Getter and Setters ----
	
	public String getDate() {
		return Date;
	}
	
	public void setDate(String date) {
		Date = date;
	}
	
	public String getMessage() {
		return Message;
	}
	
	public void setMessage(String message) {
		Message = message;
	}
	
	public String getAuthor() {
		return Author;
	}
}
