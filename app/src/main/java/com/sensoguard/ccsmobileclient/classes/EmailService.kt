package com.sensoguard.ccsmobileclient.classes

import android.util.Log
import java.util.Calendar
import java.util.Properties
import javax.mail.Address
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailService(private val server: String, private val port: Int) {

    data class Email(
        val auth: Authenticator,
        val toList: List<InternetAddress>,
        val from: Address,
        val subject: String,
        val body: String
    )

    class UserPassAuthenticator(private val username: String, private val password: String) :
        Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password)
        }
    }

    fun send(email: Email, isSSL: Boolean) {
        //the values of the properties must be strings (other values are ignored by javax.mail)
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.user"] = email.from.toString()
        props["mail.smtp.host"] = server
        props["mail.smtp.port"] = port.toString()
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.ssl.enable"] = isSSL.toString()
        //verify the certificate of the mail server (protect the password of the account)
        props["mail.smtp.ssl.checkserveridentity"] = "true"
        props["mail.mime.charset"] = "UTF-8"

        try {
            // Bugs fixed: Replace  Session.getDefaultInstance because it save previous data of properties
            val msg: Message = MimeMessage(Session.getInstance(props, email.auth))
            msg.setFrom(email.from)
            msg.sentDate = Calendar.getInstance().time
            msg.setRecipients(Message.RecipientType.TO, email.toList.toTypedArray())
            msg.replyTo = arrayOf(email.from)

            msg.addHeader("X-Mailer", CLIENT_NAME)
            msg.addHeader("Precedence", "bulk")

            msg.subject = email.subject

            msg.setContent(MimeMultipart().apply {
                addBodyPart(MimeBodyPart().apply {
                    setText(email.body, "UTF-8")
                })
            })

            Transport.send(msg)
        } catch (ex: MessagingException) {
            Log.e(TAG, "failed to send the email", ex)
        }
    }

    companion object {
        private const val TAG = "EmailService"
        const val CLIENT_NAME = "Android StackOverflow programmatic email"
    }
}
