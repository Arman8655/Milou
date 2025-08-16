package aut.ap.model;

import jakarta.persistence.*;
import java.time.LocalDate;


@Entity
@Table(name = "emails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @Basic(optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;


    @Basic(optional = false)
    private String subject;


    @Basic(optional = false)
    private String body;


    @Basic(optional = false)
    private LocalDate timestamp;


    @Basic(optional = false)
    @Column(name = "email_code", unique = true, length = 6)
    private String emailCode;

    public Email() {}


    public Email(User sender, String subject, String body , LocalDate timestamp, String emailCode) {
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.timestamp = timestamp;
        this.emailCode = emailCode;
    }

    public Integer getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }
    public String getBody() {
        return body;
    }
    public LocalDate getTimestamp() {
        return timestamp;
    }
    public String getEmailCode() {
        return emailCode;
    }
    public void setSender(User sender) {
        this.sender = sender;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public void setBody(String body) {
        this.body = body;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }
}

