/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jun 9, 2005
 *
 */
package edu.umd.cs.marmoset.utilities;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author jspacco
 *
 */
public class JavaMail
{
    public static void sendMessage(String to, String from, String host, String subject, String messageText)
    throws MessagingException
    {
//      Get system properties
        Properties props = System.getProperties();
        
        //     Setup mail server
        props.put("mail.smtp.host", host);
        // imaps://junkfood.cs.umd.edu/INBOX
        
        //     Get session
        //Authenticator authenticator = 
        Session session = Session.getDefaultInstance(props, null);
        
        //     Define message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, 
                new InternetAddress(to));
        message.setSubject(subject);
        message.setText(messageText);
        
        //     Send message
        Transport.send(message);
    }
    
    public static void main(String args[])
    throws MessagingException
    {
        String host = "smtp.cs.umd.edu";
        String from = "jspacco@cs.umd.edu";
        String to = "jspacco@cs.umd.edu";
        
        sendMessage(to, from, host, "Sure this thing is on?", "We're Spinal Tap from the UK!");
        System.out.println("It thinks that it sent something!");
    }
}
