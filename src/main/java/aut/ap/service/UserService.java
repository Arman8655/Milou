package aut.ap.service;
import aut.ap.model.User;
import java.util.Scanner;
import static aut.ap.framework.SingletonSessionFactory.getSessionFactory;


public class UserService {

    public static void signup() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        System.out.println();

        if (name.isEmpty()){
            System.out.println("Name cannot be empty");
            System.out.println();
            return;
        }

        if (email.isEmpty()){
            System.out.println("Email cannot be empty");
            System.out.println();
            return;
        }

        if (password.length() < 8) {
            System.out.println("Password must be at least 8 characters.");
            System.out.println();
            return;
        }

        if (!email.contains("@")) {
            email += "@milou.com";
        }

        String finalEmail = email;
        User existingUser = getUserByEmail(finalEmail);

        if(existingUser != null){
            System.out.println("This email is already in use.");
            System.out.println();
            return;
        }

        getSessionFactory().inTransaction(session -> {
            User user = new User(name, finalEmail, password);
            session.persist(user);
        });

        System.out.println("Your new account is created.");
        System.out.println("Go ahead and login!");
        System.out.println();
    }

    public static User login() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (!email.contains("@")) {
            email += "@milou.com";
        }

        String finalEmail = email;
        User user = getUserByEmailAndPassword(finalEmail, password);

        if (user == null) {
            System.out.println();
            System.out.println("Email or password is incorrect.");
            System.out.println();
            return null;
        }

        System.out.println();
        System.out.println("Welcome back, " + user.getName() + "!");
        EmailRecipientService.showUnreadEmails(user);
        return user;
    }

    public static User getUserByEmail(String email) {
        return getSessionFactory().fromTransaction(session ->
                session.createQuery("select u from User u where u.email = :email", User.class)
                        .setParameter("email", email).setMaxResults(1).uniqueResult());
    }

    private static User getUserByEmailAndPassword(String email, String password) {
        return getSessionFactory().fromTransaction(session ->
                session.createQuery("select u from User u where u.email = :email and u.password = :password", User.class)
                        .setParameter("email", email).setParameter("password", password).setMaxResults(1).uniqueResult());
    }
}