package main;

import config.config;
import java.util.Map;
import java.util.List;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            config db = new config();
            
            System.out.println("=== BUS RESERVATION SYSTEM ===");
            
            int choice = -1;
            do {
                System.out.println("\n1. Register");
                System.out.println("2. Login");
                System.out.println("0. Exit");
                System.out.print("Choose option: ");
                try {
                    choice = Integer.parseInt(sc.nextLine().trim());
                } catch (Exception e) {
                    choice = -1;
                }
                
                switch (choice) {
                    case 1:
                        register(sc, db);
                        break;
                    case 2:
                        if (login(sc, db)) {
                            mainMenu(sc, db);
                        }
                        break;
                    case 0:
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            } while (choice != 0);
        }
    }

    // -------------------------
    // Register
    // -------------------------
    private static void register(Scanner sc, config db) {
        System.out.println("\n--- REGISTER ---");
        System.out.print("Name: ");
        String name = sc.nextLine().trim();

        String email = "";
        while (true) {
            System.out.print("Email: ");
            email = sc.nextLine().trim();
            if (email.isEmpty()) {
                System.out.println("Email cannot be empty.");
                continue;
            }

            // Check duplicate using fetchRecords
            String checkSql = "SELECT * FROM users WHERE email = ?";
            List<Map<String, Object>> existing = db.fetchRecords(checkSql, email);
            if (existing != null && !existing.isEmpty()) {
                System.out.println("Email already registered. Try a different email.");
            } else {
                break;
            }
        }

        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        String hashed = config.hashPassword(password);

        System.out.print("User Type (admin/passenger) [default passenger]: ");
        String userType = sc.nextLine().trim();
        if (userType == null || userType.isEmpty()) userType = "passenger";

        String status = "active";

        String insert = "INSERT INTO users(name, email, password, user_type, status) VALUES(?,?,?,?,?)";
        db.addRecord(insert, name, email, hashed, userType, status);

        System.out.println("Registration complete. Please login.");
    }

    // -------------------------
    // Login
    // -------------------------
    private static boolean login(Scanner sc, config db) {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Email: ");
        String email = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        String hashed = config.hashPassword(password);

        String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND status = 'active'";
        List<Map<String, Object>> result = db.fetchRecords(sql, email, hashed);

        if (result != null && !result.isEmpty()) {
            Map<String, Object> user = result.get(0);
            System.out.println("Welcome, " + (user.get("name") != null ? user.get("name").toString() : "User")
                    + " (" + (user.get("user_type") != null ? user.get("user_type").toString() : "passenger") + ")");
            return true;
        } else {
            System.out.println("Invalid credentials or account inactive.");
            return false;
        }
    }

    // -------------------------
    // Main CRUD Menu
    // -------------------------
    private static void mainMenu(Scanner sc, config db) {
        int opt = -1;
        do {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Manage Bus");
            System.out.println("2. Manage Passenger");
            System.out.println("3. Manage Schedule");
            System.out.println("4. Manage Ticket");
            System.out.println("5. View Users");
            System.out.println("0. Logout");
            System.out.print("Choose option: ");
            try {
                opt = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                opt = -1;
            }

            switch (opt) {
                case 1:
                    manageBus(sc, db);
                    break;
                case 2:
                    managePassenger(sc, db);
                    break;
                case 3:
                    manageSchedule(sc, db);
                    break;
                case 4:
                    manageTicket(sc, db);
                    break;
                case 5:
                    viewUsers(db);
                    break;
                case 0:
                    System.out.println("Logged out.");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (opt != 0);
    }

    // -------------------------
    // BUS CRUD
    // -------------------------
    private static void manageBus(Scanner sc, config db) {
        int ch = -1;
        do {
            System.out.println("\n--- BUS ---");
            System.out.println("1. Add Bus");
            System.out.println("2. View Buses");
            System.out.println("3. Update Bus");
            System.out.println("4. Delete Bus");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            try {
                ch = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                ch = -1;
            }

            switch (ch) {
                case 1:
                    System.out.print("Bus Number: ");
                    String busNum = sc.nextLine().trim();
                    System.out.print("Capacity: ");
                    int cap = Integer.parseInt(sc.nextLine().trim());
                    db.addRecord("INSERT INTO bus(bus_number, capacity) VALUES(?,?)", busNum, cap);
                    break;
                case 2:
                    String[] headersB = {"Bus ID", "Bus Number", "Capacity"};
                    String[] colsB = {"bus_id", "bus_number", "capacity"};
                    db.viewRecords("SELECT * FROM bus", headersB, colsB);
                    break;
                case 3:
                    System.out.print("Enter Bus ID to update: ");
                    int busId = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("New Bus Number: ");
                    String newNum = sc.nextLine().trim();
                    System.out.print("New Capacity: ");
                    int newCap = Integer.parseInt(sc.nextLine().trim());
                    db.updateRecord("UPDATE bus SET bus_number = ?, capacity = ? WHERE bus_id = ?", newNum, newCap, busId);
                    break;
                case 4:
                    System.out.print("Enter Bus ID to delete: ");
                    int delBus = Integer.parseInt(sc.nextLine().trim());
                    db.deleteRecord("DELETE FROM bus WHERE bus_id = ?", delBus);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (ch != 0);
    }

    // -------------------------
    // PASSENGER CRUD
    // -------------------------
    private static void managePassenger(Scanner sc, config db) {
        int ch = -1;
        do {
            System.out.println("\n--- PASSENGER ---");
            System.out.println("1. Add Passenger");
            System.out.println("2. View Passengers");
            System.out.println("3. Update Passenger");
            System.out.println("4. Delete Passenger");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            try {
                ch = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                ch = -1;
            }

            switch (ch) {
                case 1:
                    System.out.print("Name: ");
                    String name = sc.nextLine().trim();
                    System.out.print("Gender: ");
                    String gender = sc.nextLine().trim();
                    db.addRecord("INSERT INTO passenger(name, gender) VALUES(?,?)", name, gender);
                    break;
                case 2:
                    String[] headersP = {"Passenger ID", "Name", "Gender"};
                    String[] colsP = {"passenger_id", "name", "gender"};
                    db.viewRecords("SELECT * FROM passenger", headersP, colsP);
                    break;
                case 3:
                    System.out.print("Enter Passenger ID to update: ");
                    int pId = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("New Name: ");
                    String nName = sc.nextLine().trim();
                    System.out.print("New Gender: ");
                    String nGender = sc.nextLine().trim();
                    db.updateRecord("UPDATE passenger SET name = ?, gender = ? WHERE passenger_id = ?", nName, nGender, pId);
                    break;
                case 4:
                    System.out.print("Enter Passenger ID to delete: ");
                    int delP = Integer.parseInt(sc.nextLine().trim());
                    db.deleteRecord("DELETE FROM passenger WHERE passenger_id = ?", delP);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (ch != 0);
    }

    // -------------------------
    // SCHEDULE CRUD
    // -------------------------
    private static void manageSchedule(Scanner sc, config db) {
        int ch = -1;
        do {
            System.out.println("\n--- SCHEDULE ---");
            System.out.println("1. Add Schedule");
            System.out.println("2. View Schedules");
            System.out.println("3. Update Schedule");
            System.out.println("4. Delete Schedule");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            try {
                ch = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                ch = -1;
            }

            switch (ch) {
                case 1:
                    System.out.print("Bus ID: ");
                    int busId = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Departure: ");
                    String dep = sc.nextLine().trim();
                    System.out.print("Arrival: ");
                    String arr = sc.nextLine().trim();
                    System.out.print("Route: ");
                    String route = sc.nextLine().trim();
                    db.addRecord("INSERT INTO schedule(bus_id, departure, arrival, route) VALUES(?,?,?,?)", busId, dep, arr, route);
                    break;
                case 2:
                    String[] headersS = {"Schedule ID", "Bus ID", "Departure", "Arrival", "Route"};
                    String[] colsS = {"schedule_id", "bus_id", "departure", "arrival", "route"};
                    db.viewRecords("SELECT * FROM schedule", headersS, colsS);
                    break;
                case 3:
                    System.out.print("Enter Schedule ID to update: ");
                    int sId = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("New Departure: ");
                    String nd = sc.nextLine().trim();
                    System.out.print("New Arrival: ");
                    String na = sc.nextLine().trim();
                    System.out.print("New Route: ");
                    String nr = sc.nextLine().trim();
                    db.updateRecord("UPDATE schedule SET departure = ?, arrival = ?, route = ? WHERE schedule_id = ?", nd, na, nr, sId);
                    break;
                case 4:
                    System.out.print("Enter Schedule ID to delete: ");
                    int delS = Integer.parseInt(sc.nextLine().trim());
                    db.deleteRecord("DELETE FROM schedule WHERE schedule_id = ?", delS);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (ch != 0);
    }

    // -------------------------
    // TICKET CRUD
    // -------------------------
    private static void manageTicket(Scanner sc, config db) {
        int ch = -1;
        do {
            System.out.println("\n--- TICKET ---");
            System.out.println("1. Add Ticket");
            System.out.println("2. View Tickets");
            System.out.println("3. Update Ticket");
            System.out.println("4. Delete Ticket");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            try {
                ch = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                ch = -1;
            }

            switch (ch) {
                case 1:
                    System.out.print("Passenger ID: ");
                    int pid = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Schedule ID: ");
                    int sid = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Seat Number: ");
                    String seat = sc.nextLine().trim();
                    db.addRecord("INSERT INTO ticket(passenger_id, schedule_id, seat_number) VALUES(?,?,?)", pid, sid, seat);
                    break;
                case 2:
                    String[] headersT = {"Ticket ID", "Passenger ID", "Schedule ID", "Seat"};
                    String[] colsT = {"ticket_id", "passenger_id", "schedule_id", "seat_number"};
                    db.viewRecords("SELECT * FROM ticket", headersT, colsT);
                    break;
                case 3:
                    System.out.print("Enter Ticket ID to update: ");
                    int tId = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("New Seat Number: ");
                    String ns = sc.nextLine().trim();
                    db.updateRecord("UPDATE ticket SET seat_number = ? WHERE ticket_id = ?", ns, tId);
                    break;
                case 4:
                    System.out.print("Enter Ticket ID to delete: ");
                    int delT = Integer.parseInt(sc.nextLine().trim());
                    db.deleteRecord("DELETE FROM ticket WHERE ticket_id = ?", delT);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (ch != 0);
    }

    // -------------------------
    // View Users (admin/debug)
    // -------------------------
    private static void viewUsers(config db) {
        String[] headers = {"User ID", "Name", "Email", "Type", "Status"};
        String[] cols = {"user_id", "name", "email", "user_type", "status"};
        db.viewRecords("SELECT user_id, name, email, user_type, status FROM users", headers, cols);
    }
}
