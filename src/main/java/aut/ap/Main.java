package aut.ap;
import aut.ap.framework.SingletonSessionFactory;
import aut.ap.model.User;
import aut.ap.service.EmailRecipientService;
import aut.ap.service.UserService;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("[L]ogin, [S]ign up, [E]xit: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("l")) {
                User user = UserService.login();
                if (user != null) {
                    EmailRecipientService.showMainMenu(user);
                }
            }

            else if (input.equals("s")) {
                UserService.signup();
            }


            else if (input.equals("e")) {
                SingletonSessionFactory.close();
                System.exit(0);
            }
        }
    }
}