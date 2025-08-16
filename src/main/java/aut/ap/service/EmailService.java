package aut.ap.service;
import aut.ap.model.Email;
import aut.ap.model.EmailRecipient;
import aut.ap.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import static aut.ap.framework.SingletonSessionFactory.getSessionFactory;

public class EmailService {
    public static void sendEmail(User user) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Recipient(s): ");
        String recipientsInput = scanner.nextLine().trim();

        System.out.print("Subject: ");
        String subject = scanner.nextLine().trim();

        System.out.print("Body: ");
        String body = scanner.nextLine().trim();

        System.out.println();

        String[] recipientEmails = recipientsInput.split(",");
        List<User> newRecipients = new ArrayList<>();
        for (String email : recipientEmails) {
            email = email.trim();

            if (email.isEmpty()) {
                continue;
            }
            if (!email.contains("@")) {
                email += "@milou.com";
            }
            User recipient = UserService.getUserByEmail(email);
            if (recipient != null) {
                newRecipients.add(recipient);
            }
        }

        if (newRecipients.isEmpty()) {
            System.out.println("No valid recipients found.");
            System.out.println();
            return;
        }

        if(subject.isEmpty()){
            System.out.println("Subject cannot be empty.");
            System.out.println();
            return;
        }

        if(body.isEmpty()){
            System.out.println("Body cannot be empty.");
            System.out.println();
            return;
        }

        String newCode = generateRandomCode();
        Email email = new Email(user, subject, body, LocalDate.now(), newCode);

        getSessionFactory().inTransaction(session -> {
            session.persist(email);
        });

        for (User recipient : newRecipients) {
            EmailRecipientService.createEmailRecipient(email, recipient);
        }

        System.out.println("Successfully sent your email.");
        System.out.println("Code: " + newCode);

        System.out.println();
    }

    public static void replyToEmail(User user) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Code: ");
        String code = scanner.nextLine().trim();

        System.out.print("Body: ");
        String body = scanner.nextLine().trim();

        System.out.println();

        EmailRecipient emailRecipient = EmailRecipientService.getEmailRecipientByCodeAndUser(code, user);

        if (emailRecipient == null) {
            System.out.println("You cannot reply to this email.");
            System.out.println();
            return;
        }

        if(body.isEmpty()){
            System.out.println("Body cannot be empty");
            System.out.println();
            return;
        }

        Email originalEmail = emailRecipient.getEmail();
        String newCode = generateRandomCode();
        Email replyEmail = new Email(user, "[Re] " + originalEmail.getSubject(), body, LocalDate.now(), newCode);

        getSessionFactory().inTransaction(session -> {
            session.persist(replyEmail);
        });

        List<User> uniqueRecipients = EmailRecipientService.getUniqueRecipientsForReply(originalEmail, user);


        if (uniqueRecipients.isEmpty()) {
            System.out.println("No valid recipients found for reply.");
            System.out.println();
            return;
        }

        for (User recipient : uniqueRecipients) {
            EmailRecipientService.createEmailRecipient(replyEmail, recipient);
        }

        System.out.println("Successfully sent your reply to email " + code + ".");
        System.out.println("Code: " + newCode);

        System.out.println();
    }

    public static void forwardEmail(User user) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Code: ");
        String code = scanner.nextLine().trim();

        System.out.print("Recipient(s): ");
        String recipientsInput = scanner.nextLine().trim();

        System.out.println();

        Email originalEmail = EmailRecipientService.getEmailByCodeForUser(code, user);
        if (originalEmail == null) {
            System.out.println("You cannot forward this email.");
            System.out.println();
            return;
        }

        String[] recipientEmails = recipientsInput.split(",");
        List<User> newRecipients = new ArrayList<>();
        for (String email : recipientEmails) {
            email = email.trim();
            if (email.isEmpty()) {
                continue;
            }
            if (!email.contains("@")) {
                email += "@milou.com";
            }
            User recipient = UserService.getUserByEmail(email);
            if (recipient != null) {
                newRecipients.add(recipient);
            }
        }

        if (newRecipients.isEmpty()) {
            System.out.println("No valid recipients found.");
            System.out.println();
            return;
        }

        String newCode = generateRandomCode();
        Email forwardEmail = new Email(user, "[Fw] " + originalEmail.getSubject(), originalEmail.getBody(), LocalDate.now(), newCode);

        getSessionFactory().inTransaction(session -> {
            session.persist(forwardEmail);
        });

        for (User recipient : newRecipients) {
            EmailRecipientService.createEmailRecipient(forwardEmail, recipient);
        }

        System.out.println("Successfully forwarded your email.");
        System.out.println("Code: " + newCode);
        System.out.println();
    }

    public static void deleteEmail(User user) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Code: ");
        String code = scanner.nextLine().trim();

        System.out.println();

        Email email = EmailRecipientService.getEmailByCodeForUser(code, user);
        if (email == null) {
            System.out.println("You cannot delete this email.");
            System.out.println();
            return;
        }

        getSessionFactory().inTransaction(session -> {

            boolean isSender = email.getSender().equals(user);

            if (isSender) {

                session.createMutationQuery("delete from EmailRecipient er where er.email = :email")
                        .setParameter("email", email)
                        .executeUpdate();
                session.remove(email);
                System.out.println("Successfully deleted email with code " + code + " for all users.");
            }
            else {

                session.createMutationQuery("delete from EmailRecipient er where er.email = :email and er.receiver = :user")
                        .setParameter("email", email)
                        .setParameter("user", user)
                        .executeUpdate();
                System.out.println("Successfully deleted email with code " + code + " from your inbox.");
            }
        });

        System.out.println();
    }

    private static String generateRandomCode() {
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        String code;


        do {
            StringBuilder codeBuilder = new StringBuilder();

            for (int i = 0; i < 6; i++) {
                codeBuilder.append(characters.charAt(random.nextInt(characters.length())));
            }
            code = codeBuilder.toString();

            String finalCode = code;
            Email existingEmail = getSessionFactory().fromTransaction(session ->
                    session.createQuery("select e from Email e where e.emailCode = :code", Email.class)
                            .setParameter("code", finalCode).setMaxResults(1).uniqueResult());

            if (existingEmail == null) {
                break;
            }
        }

        while (true);

        return code;
    }
}