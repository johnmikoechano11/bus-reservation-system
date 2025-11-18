package main;

import config.config;
import java.util.List;
import java.util.Map;
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
                        Map<String, Object> user = login(sc, db);
                        if (user != null) {
                            if ("admin".equals(user.get("user_type"))) {
                                adminMenu(sc, db);
                            } else {
                                passengerMenu(sc, db, (int) user.get("user_id"));
                            }
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

    // ============================
    // REGISTER
    // ============================
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

            List<Map<String, Object>> existing =
                db.fetchRecords("SELECT * FROM users WHERE email = ?", email);

            if (existing != null && !existing.isEmpty()) {
                System.out.println("Email already registered. Try again.");
            } else {
                break;
            }
        }

        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        String hashed = config.hashPassword(password);

        System.out.print("User Type (admin/passenger) [default passenger]: ");
        String type = sc.nextLine().trim();
        if (type.isEmpty()) type = "passenger";

        db.addRecord(
            "INSERT INTO users(name, email, password, user_type, status) VALUES(?,?,?,?,?)",
            name, email, hashed, type, "active"
        );

        System.out.println("Registration complete as " + type + "!");
    }

    // ============================
    // LOGIN
    // ============================
    private static Map<String, Object> login(Scanner sc, config db) {

        System.out.println("\n--- LOGIN ---");

        System.out.print("Email: ");
        String email = sc.nextLine().trim();

        System.out.print("Password: ");
        String pass = sc.nextLine().trim();
        String hashed = config.hashPassword(pass);

        String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND status = 'active'";
        List<Map<String, Object>> result = db.fetchRecords(sql, email, hashed);

        if (result != null && !result.isEmpty()) {
            Map<String, Object> user = result.get(0);
            System.out.println("\n✅ Welcome, " + user.get("name") +
                    " (" + user.get("user_type") + ")");
            return user;
        } else {
            System.out.println("❌ Invalid credentials.");
            return null;
        }
    }

    // ============================
    // ADMIN MENU
    // ============================
    private static void adminMenu(Scanner sc, config db) {
        int opt = -1;
        do {
            System.out.println("\n=== ADMIN MENU ===");
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
                case 1: manageBus(sc, db); break;
                case 2: managePassenger(sc, db); break;
                case 3: manageSchedule(sc, db); break;
                case 4: manageTicket(sc, db); break;
                case 5: viewUsers(db); break;
                case 0: System.out.println("Logged out."); break;
                default: System.out.println("Invalid option."); break;
            }

        } while (opt != 0);
    }

    // ============================
    // PASSENGER MENU
    // ============================
    private static void passengerMenu(Scanner sc, config db, int userId) {
        int opt = -1;
        do {
            System.out.println("\n=== PASSENGER MENU ===");
            System.out.println("1. View Available Buses");
            System.out.println("2. View Schedule");
            System.out.println("3. Book Ticket");
            System.out.println("4. View My Tickets");
            System.out.println("0. Logout");
            System.out.print("Choose option: ");

            try {
                opt = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                opt = -1;
            }

            switch (opt) {
                case 1:
                    db.viewRecords("SELECT * FROM bus",
                        new String[]{"Bus ID","Bus Number","Capacity","Type","Route","Driver"},
                        new String[]{"bus_id","bus_number","capacity","bus_type","route","driver_name"});
                    break;

                case 2:
                    db.viewRecords("SELECT * FROM schedule",
                        new String[]{"Schedule ID","Bus ID","Departure","Arrival","Route"},
                        new String[]{"schedule_id","bus_id","departure","arrival","route"});
                    break;

                case 3:
                    System.out.print("Seat Number: ");
                    String seat = sc.nextLine().trim();
                    System.out.print("Route: ");
                    String route = sc.nextLine().trim();
                    System.out.print("Date Booked (YYYY-MM-DD): ");
                    String dbook = sc.nextLine().trim();

                    db.addRecord(
                        "INSERT INTO ticket(user_id, seat_number, route, date_booked) VALUES(?,?,?,?)",
                        userId, seat, route, dbook
                    );
                    System.out.println("✅ Ticket booked!");
                    break;

                case 4:
                    db.viewRecords("SELECT * FROM ticket WHERE user_id=?",
                        new String[]{"Ticket ID","Seat","Route","Booked"},
                        new String[]{"ticket_id","seat_number","route","date_booked"}, userId);
                    break;

                case 0:
                    System.out.println("Logged out."); break;
                default:
                    System.out.println("Invalid option."); break;
            }

        } while (opt != 0);
    }

    // ============================
    // BUS CRUD
    // ============================
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

            try { ch = Integer.parseInt(sc.nextLine().trim()); } catch (Exception e) { ch=-1; }

            switch(ch) {
                case 1:
                    System.out.print("Bus Number: "); String bn = sc.nextLine().trim();
                    System.out.print("Capacity: "); int cap = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Bus Type: "); String bt = sc.nextLine().trim();
                    System.out.print("Route: "); String route = sc.nextLine().trim();
                    System.out.print("Driver Name: "); String dn = sc.nextLine().trim();

                    db.addRecord("INSERT INTO bus(bus_number, capacity, bus_type, route, driver_name) VALUES(?,?,?,?,?)",
                        bn, cap, bt, route, dn);
                    break;

                case 2:
                    db.viewRecords("SELECT * FROM bus",
                        new String[]{"Bus ID","Bus Number","Capacity","Type","Route","Driver"},
                        new String[]{"bus_id","bus_number","capacity","bus_type","route","driver_name"});
                    break;

                case 3:
                    System.out.print("Bus ID to update: "); int bid = Integer.parseInt(sc.nextLine().trim());
                    List<Map<String,Object>> existing = db.fetchRecords("SELECT * FROM bus WHERE bus_id=?", bid);
                    if (existing.isEmpty()) { System.out.println("❌ Bus ID does not exist!"); break; }

                    System.out.print("New Bus Number: "); String nbn = sc.nextLine().trim();
                    System.out.print("New Capacity: "); int ncap = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("New Type: "); String nbt = sc.nextLine().trim();
                    System.out.print("New Route: "); String nr = sc.nextLine().trim();
                    System.out.print("New Driver: "); String nd = sc.nextLine().trim();

                    db.updateRecord("UPDATE bus SET bus_number=?, capacity=?, bus_type=?, route=?, driver_name=? WHERE bus_id=?",
                        nbn, ncap, nbt, nr, nd, bid);
                    break;

                case 4:
                    System.out.print("Bus ID to delete: "); int dbus = Integer.parseInt(sc.nextLine().trim());
                    existing = db.fetchRecords("SELECT * FROM bus WHERE bus_id=?", dbus);
                    if (existing.isEmpty()) { System.out.println("❌ Bus ID does not exist!"); break; }

                    db.deleteRecord("DELETE FROM bus WHERE bus_id=?", dbus);
                    break;

                case 0: break;
                default: System.out.println("Invalid option."); break;
            }

        } while(ch!=0);
    }

    // ============================
    // PASSENGER CRUD (with ID check)
    // ============================
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

            try { ch = Integer.parseInt(sc.nextLine().trim()); } catch (Exception e) { ch=-1; }

            switch(ch) {
                case 1:
                    System.out.print("Name: "); String n = sc.nextLine().trim();
                    System.out.print("Gender: "); String g = sc.nextLine().trim();
                    db.addRecord("INSERT INTO passenger(name, gender) VALUES(?,?)", n, g);
                    break;

                case 2:
                    db.viewRecords("SELECT * FROM passenger",
                        new String[]{"Passenger ID","Name","Gender"},
                        new String[]{"passenger_id","name","gender"});
                    break;

                case 3:
                    System.out.print("Passenger ID: "); int pid = Integer.parseInt(sc.nextLine().trim());
                    List<Map<String,Object>> existing = db.fetchRecords("SELECT * FROM passenger WHERE passenger_id=?", pid);
                    if (existing.isEmpty()) { System.out.println("❌ Passenger ID does not exist!"); break; }

                    System.out.print("New Name: "); String nn = sc.nextLine().trim();
                    System.out.print("New Gender: "); String ng = sc.nextLine().trim();
                    db.updateRecord("UPDATE passenger SET name=?, gender=? WHERE passenger_id=?", nn, ng, pid);
                    break;

                case 4:
                    System.out.print("Passenger ID to delete: "); int dp = Integer.parseInt(sc.nextLine().trim());
                    existing = db.fetchRecords("SELECT * FROM passenger WHERE passenger_id=?", dp);
                    if (existing.isEmpty()) { System.out.println("❌ Passenger ID does not exist!"); break; }
                    db.deleteRecord("DELETE FROM passenger WHERE passenger_id=?", dp);
                    break;

                case 0: break;
                default: System.out.println("Invalid option."); break;
            }

        } while(ch!=0);
    }

    // ============================
    // SCHEDULE CRUD
    // ============================
    private static void manageSchedule(Scanner sc, config db) {
        int ch=-1;
        do {
            System.out.println("\n--- SCHEDULE ---");
            System.out.println("1. Add Schedule");
            System.out.println("2. View Schedules");
            System.out.println("3. Update Schedule");
            System.out.println("4. Delete Schedule");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            try { ch = Integer.parseInt(sc.nextLine().trim()); } catch(Exception e){ch=-1;}

            switch(ch) {
                case 1:
                    System.out.print("Bus ID: "); int bid = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Departure: "); String dep = sc.nextLine().trim();
                    System.out.print("Arrival: "); String arr = sc.nextLine().trim();
                    System.out.print("Route: "); String rt = sc.nextLine().trim();

                    db.addRecord("INSERT INTO schedule(bus_id, departure, arrival, route) VALUES(?,?,?,?)", bid, dep, arr, rt);
                    break;

                case 2:
                    db.viewRecords("SELECT * FROM schedule",
                        new String[]{"Schedule ID","Bus ID","Departure","Arrival","Route"},
                        new String[]{"schedule_id","bus_id","departure","arrival","route"});
                    break;

                case 3:
                    System.out.print("Schedule ID: "); int sid = Integer.parseInt(sc.nextLine().trim());
                    List<Map<String,Object>> existing = db.fetchRecords("SELECT * FROM schedule WHERE schedule_id=?", sid);
                    if(existing.isEmpty()){ System.out.println("❌ Schedule ID does not exist!"); break; }

                    System.out.print("New Bus ID: "); int nbid = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("New Departure: "); String ndep = sc.nextLine().trim();
                    System.out.print("New Arrival: "); String narr = sc.nextLine().trim();
                    System.out.print("New Route: "); String nrt = sc.nextLine().trim();

                    db.updateRecord("UPDATE schedule SET bus_id=?, departure=?, arrival=?, route=? WHERE schedule_id=?",
                        nbid, ndep, narr, nrt, sid);
                    break;

                case 4:
                    System.out.print("Schedule ID to delete: "); int ds = Integer.parseInt(sc.nextLine().trim());
                    existing = db.fetchRecords("SELECT * FROM schedule WHERE schedule_id=?", ds);
                    if(existing.isEmpty()){ System.out.println("❌ Schedule ID does not exist!"); break; }

                    db.deleteRecord("DELETE FROM schedule WHERE schedule_id=?", ds);
                    break;

                case 0: break;
                default: System.out.println("Invalid option."); break;
            }

        } while(ch!=0);
    }

    // ============================
    // TICKET CRUD
    // ============================
    private static void manageTicket(Scanner sc, config db) {
        int ch=-1;
        do {
            System.out.println("\n--- TICKET ---");
            System.out.println("1. Book Ticket");
            System.out.println("2. View Tickets");
            System.out.println("3. Update Ticket");
            System.out.println("4. Delete Ticket");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            try { ch=Integer.parseInt(sc.nextLine().trim()); } catch(Exception e){ch=-1;}

            switch(ch) {
                case 1:
                    System.out.print("User ID: "); int uid = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Seat Number: "); String seat = sc.nextLine().trim();
                    System.out.print("Route: "); String route = sc.nextLine().trim();
                    System.out.print("Date Booked (YYYY-MM-DD): "); String dbook = sc.nextLine().trim();
                    db.addRecord("INSERT INTO ticket(user_id, seat_number, route, date_booked) VALUES(?,?,?,?)", uid, seat, route, dbook);
                    System.out.println("✅ Ticket booked!");
                    break;

                case 2:
                    db.viewRecords("SELECT * FROM ticket",
                        new String[]{"Ticket ID","User ID","Seat","Route","Date Booked"},
                        new String[]{"ticket_id","user_id","seat_number","route","date_booked"});
                    break;

                case 3:
                    System.out.print("Ticket ID: "); int tid = Integer.parseInt(sc.nextLine().trim());
                    List<Map<String,Object>> existing = db.fetchRecords("SELECT * FROM ticket WHERE ticket_id=?", tid);
                    if(existing.isEmpty()){ System.out.println("❌ Ticket ID does not exist!"); break; }

                    System.out.print("New Seat Number: "); String ns = sc.nextLine().trim();
                    System.out.print("New Route: "); String nr = sc.nextLine().trim();
                    System.out.print("New Date Booked (YYYY-MM-DD): "); String nd = sc.nextLine().trim();

                    db.updateRecord("UPDATE ticket SET seat_number=?, route=?, date_booked=? WHERE ticket_id=?", ns, nr, nd, tid);
                    System.out.println("✅ Ticket updated!");
                    break;

                case 4:
                    System.out.print("Ticket ID to delete: "); int dt = Integer.parseInt(sc.nextLine().trim());
                    existing = db.fetchRecords("SELECT * FROM ticket WHERE ticket_id=?", dt);
                    if(existing.isEmpty()){ System.out.println("❌ Ticket ID does not exist!"); break; }

                    db.deleteRecord("DELETE FROM ticket WHERE ticket_id=?", dt);
                    System.out.println("✅ Ticket deleted!");
                    break;

                case 0: break;
                default: System.out.println("Invalid option."); break;
            }

        } while(ch!=0);
    }

    // ============================
    // VIEW USERS
    // ============================
    private static void viewUsers(config db) {
        db.viewRecords(
            "SELECT user_id, name, email, user_type, status FROM users",
            new String[]{"User ID", "Name", "Email", "Type", "Status"},
            new String[]{"user_id","name","email","user_type","status"}
        );
    }
}
