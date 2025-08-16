package aut.ap.model;

import jakarta.persistence.*;


@Entity
@Table(name = "email_recipients")
public class EmailRecipient {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @ManyToOne
    @Basic(optional = false)
    @JoinColumn(name = "email_id")
    private Email email;

    @ManyToOne
    @Basic(optional = false)
    @JoinColumn(name = "receiver_id")
    private User receiver;


    @Basic(optional = false)
    @Column(name = "is_read")
    private boolean isRead = false;


    public EmailRecipient() {
    }


    public EmailRecipient(Email email, User receiver, boolean isRead) {
        this.email = email;
        this.receiver = receiver;
        this.isRead = isRead;
    }

    public Integer getId() {
        return id;
    }
    public Email getEmail() {
        return email;
    }

    public User getReceiver() {
        return receiver;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
}



