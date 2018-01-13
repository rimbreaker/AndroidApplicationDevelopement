package dev.jkkj.chatapp;

import java.util.Date;

/**
 * Created by Jan on 09.01.2018.
 */

public class ChatMessage {
    private String messageText;
    private String messageUser;
    private long messageTime;

    //used creator
    public ChatMessage(String messageText, String messageUser) {
        this.messageText = messageText;
        this.messageUser = messageUser;

        messageTime = new Date().getTime();
    }


    //default creator(don't delete; it is needed for Firebase)
    public ChatMessage() {
    }

    //all the setters and getters
    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
