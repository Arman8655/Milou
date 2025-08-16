package aut.ap.service;
import aut.ap.model.Email;
import aut.ap.model.EmailRecipient;
import aut.ap.model.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import static aut.ap.framework.SingletonSessionFactory.getSessionFactory;

public class EmailRecipientService {
    public static void showMainMenu(User user) {
        Scanner scanner = new Scanner(System.in);


        while (true) {
            System.out.print("[S]end, [V]iew, [R]eply, [F]orward, [L]ogout: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("s")) {
                EmailService.sendEmail(user);
            }
            else if (input.equals("v")) {
                showViewMenu(user);
            }
            else if (input.equals("r")) {
                EmailService.replyToEmail(user);
            }
            else if (input.equals("f")) {
                EmailService.forwardEmail(user);
            }

            else if (input.equals("l")) {
                break;
            }
        }
    }

    private static void showViewMenu(User user) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("[A]ll emails, [U]nread emails, [S]ent emails, [R]ead by code, [B]ack: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("a")) {
                showAllEmails(user);
            }
            else if (input.equals("u")) {
                showUnreadEmails(user);
            }
            else if (input.equals("s")) {
                showSentEmails(user);
            }
            else if (input.equals("r")) {
                readEmailByCode(user);
            }
            else if (input.equals("b")) {
                break;
            }
        }
    }

    public static void showAllEmails(User user) {
        List<EmailRecipient> emailRecipients = getSessionFactory().fromTransaction(session ->
                session.createQuery("select er from EmailRecipient er "
                                + "where er.receiver = :user "
                                + "order by er.email.timestamp desc", EmailRecipient.class)
                        .setParameter("user", user).getResultList());

        System.out.println();

        System.out.println("All Emails:");
        if (emailRecipients.isEmpty()) {
            System.out.println("No emails found.");
        }
        else {
            for (EmailRecipient er : emailRecipients) {
                Email email = er.getEmail();
                System.out.println("+ " + email.getSender().getEmail() + " - " + email.getSubject() + " (" + email.getEmailCode() + ")");
            }
        }

        System.out.println();
    }

    public static void showUnreadEmails(User user) {
        List<EmailRecipient> emailRecipients = getSessionFactory().fromTransaction(session ->
                session.createQuery("select er from EmailRecipient er "
                                + "where er.receiver = :user and er.isRead = false "
                                + "order by er.email.timestamp desc", EmailRecipient.class)
                        .setParameter("user", user).getResultList());

        System.out.println();

        System.out.println("Unread Emails:");
        System.out.println();
        System.out.println(emailRecipients.size() + " unread emails:");
        if (emailRecipients.isEmpty()) {
            System.out.println("No unread emails found.");
        }

        else {
            for (EmailRecipient er : emailRecipients) {
                Email email = er.getEmail();
                System.out.println("+ " + email.getSender().getEmail() + " - " + email.getSubject() + " (" + email.getEmailCode() + ")");
            }
        }

        System.out.println();
    }

    public static void showSentEmails(User user) {
        List<Email> emails = getSessionFactory().fromTransaction(session ->
                session.createQuery("select e from Email e "
                                + "where e.sender = :user "
                                + "order by e.timestamp desc", Email.class)
                        .setParameter("user", user).getResultList());

        System.out.println();

        System.out.println("Sent Emails:");
        if (emails.isEmpty()) {
            System.out.println("No sent emails found.");
        }

        else {
            for (Email email : emails) {
                System.out.println("+ " + getRecipientsString(email) + " - " + email.getSubject() + " (" + email.getEmailCode() + ")");
            }
        }

        System.out.println();
    }

    public static void readEmailByCode(User user) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Code: ");
        String code = scanner.nextLine().trim();

        System.out.println();

        EmailRecipient emailRecipient = getEmailRecipientByCodeAndUser(code, user);
        if (emailRecipient == null) {
            Email email = getSentEmailByCodeAndUser(code, user);
            if (email == null) {
                System.out.println("You cannot read this email.");
                System.out.println();
                return;
            }

            System.out.println("Code: " + email.getEmailCode());
            System.out.println("Recipient(s): " + getRecipientsString(email));
            System.out.println("Subject: " + email.getSubject());
            System.out.println("Date: " + email.getTimestamp());
            System.out.println();
            System.out.println("----------");
            System.out.println(email.getBody());
            System.out.println("----------");
            System.out.println();
        }

        else {
            Email email = emailRecipient.getEmail();
            System.out.println("Code: " + email.getEmailCode());
            System.out.println("Recipient(s): " + getRecipientsString(email));
            System.out.println("Subject: " + email.getSubject());
            System.out.println("Date: " + email.getTimestamp());
            System.out.println();
            System.out.println("----------");
            System.out.println(email.getBody());
            System.out.println("----------");
            System.out.println();

            getSessionFactory().inTransaction(session -> {
                emailRecipient.setRead(true);
                session.merge(emailRecipient);
            });
        }
    }

    public static void createEmailRecipient(Email email, User recipient) {
        getSessionFactory().inTransaction(session -> {
            EmailRecipient emailRecipient = new EmailRecipient(email, recipient, false);
            session.persist(emailRecipient);
        });
    }

    public static EmailRecipient getEmailRecipientByCodeAndUser(String code, User user) {
        return getSessionFactory().fromTransaction(session ->
                session.createQuery("select er from EmailRecipient er "
                                + "where er.email.emailCode = :code and er.receiver = :user", EmailRecipient.class)
                        .setParameter("code", code).setParameter("user", user).setMaxResults(1).uniqueResult());
    }

    public static Email getEmailByCodeForUser(String code, User user) {
        return getSessionFactory().fromTransaction(session ->
                session.createQuery("select e from Email e "
                                + "where e.emailCode = :code and (e.sender = :user or exists (select er from EmailRecipient er where er.email = e and er.receiver = :user))", Email.class)
                        .setParameter("code", code).setParameter("user", user).setMaxResults(1).uniqueResult());
    }

    public static Email getSentEmailByCodeAndUser(String code, User user) {
        return getSessionFactory().fromTransaction(session ->
                session.createQuery("select e from Email e "
                                + "where e.emailCode = :code and e.sender = :user", Email.class)
                        .setParameter("code", code).setParameter("user", user).setMaxResults(1).uniqueResult());
    }

    public static List<User> getUniqueRecipientsForReply(Email originalEmail, User user) {
        Set<User> uniqueRecipients = new HashSet<>();
        if (!originalEmail.getSender().equals(user)) {
            uniqueRecipients.add(originalEmail.getSender());
        }

        List<EmailRecipient> originalRecipients = getSessionFactory().fromTransaction(session ->
                session.createQuery("select er from EmailRecipient er "
                                + "where er.email = :email and er.receiver != :user", EmailRecipient.class)
                        .setParameter("email", originalEmail).setParameter("user", user).getResultList());

        for (EmailRecipient er : originalRecipients) {
            uniqueRecipients.add(er.getReceiver());
        }

        return new ArrayList<>(uniqueRecipients);
    }

    public static String getRecipientsString(Email email) {
        List<EmailRecipient> recipients = getSessionFactory().fromTransaction(session ->
                session.createQuery("select er from EmailRecipient er "
                                + "where er.email = :email", EmailRecipient.class)
                        .setParameter("email", email).getResultList());

        StringBuilder recipientEmails = new StringBuilder();


        for (int i = 0; i < recipients.size(); i++) {
            recipientEmails.append(recipients.get(i).getReceiver().getEmail());
            if (i < recipients.size() - 1) {
                recipientEmails.append(", ");
            }
        }
        return recipientEmails.toString();
    }
}