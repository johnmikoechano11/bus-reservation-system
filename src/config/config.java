package config;

import java.sql.*;
import java.util.*;

public class config {

    //-----------------------------------------------
    // DATABASE CONNECTION
    //-----------------------------------------------
    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load SQLite JDBC
            con = DriverManager.getConnection("jdbc:sqlite:busDB.db"); // Connect to DB
            System.out.println("Connection Successful");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }

    //-----------------------------------------------
    // HELPER - SET PREPARED STATEMENT VALUES
    //-----------------------------------------------
    private void setPreparedStatementValues(PreparedStatement pstmt, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            pstmt.setObject(i + 1, values[i]);
        }
    }

    //-----------------------------------------------
    // ADD RECORD
    //-----------------------------------------------
    public void addRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setPreparedStatementValues(pstmt, values);
            pstmt.executeUpdate();
            System.out.println("Record added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
        }
    }

    //-----------------------------------------------
    // UPDATE RECORD
    //-----------------------------------------------
    public void updateRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setPreparedStatementValues(pstmt, values);
            int rows = pstmt.executeUpdate();

            if (rows > 0)
                System.out.println("Record updated successfully!");
            else
                System.out.println("ID does not exist!");

        } catch (SQLException e) {
            System.out.println("Error updating record: " + e.getMessage());
        }
    }

    //-----------------------------------------------
    // DELETE RECORD
    //-----------------------------------------------
    public void deleteRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setPreparedStatementValues(pstmt, values);
            int rows = pstmt.executeUpdate();

            if (rows > 0)
                System.out.println("Record deleted successfully!");
            else
                System.out.println("ID does not exist!");

        } catch (SQLException e) {
            System.out.println("Error deleting record: " + e.getMessage());
        }
    }

    //-----------------------------------------------
    // VIEW RECORDS WITHOUT PARAMETERS
    //-----------------------------------------------
    public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames) {

        if (columnHeaders.length != columnNames.length) {
            System.out.println("Error: Mismatch between column headers and column names.");
            return;
        }

        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlQuery)) {

            printTable(rs, columnHeaders, columnNames);

        } catch (SQLException e) {
            System.out.println("Error retrieving records: " + e.getMessage());
        }
    }

    //-----------------------------------------------
    // VIEW RECORDS WITH PARAMETERS
    //-----------------------------------------------
    public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames, Object... params) {

        if (columnHeaders.length != columnNames.length) {
            System.out.println("Error: Mismatch between column headers and column names.");
            return;
        }

        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            ResultSet rs = pstmt.executeQuery();
            printTable(rs, columnHeaders, columnNames);

        } catch (SQLException e) {
            System.out.println("Error retrieving records: " + e.getMessage());
        }
    }

    //-----------------------------------------------
    // PRINT TABLE UTILITY
    //-----------------------------------------------
    private void printTable(ResultSet rs, String[] headers, String[] columns) throws SQLException {

        System.out.println("--------------------------------------------------------------------------------");
        System.out.print("| ");

        for (String h : headers) {
            System.out.printf("%-20s | ", h);
        }
        System.out.println("\n--------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.print("| ");
            for (String col : columns) {
                String value = rs.getString(col);
                System.out.printf("%-20s | ", value != null ? value : "");
            }
            System.out.println();
        }

        System.out.println("--------------------------------------------------------------------------------");
    }

    //-----------------------------------------------
    // FETCH RECORDS INTO LIST<MAP>
    //-----------------------------------------------
    public List<Map<String, Object>> fetchRecords(String sqlQuery, Object... values) {
        List<Map<String, Object>> records = new ArrayList<>();

        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }

            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= count; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                records.add(row);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching records: " + e.getMessage());
        }

        return records;
    }

    //-----------------------------------------------
    // HASH PASSWORD
    //-----------------------------------------------
    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashed) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();

        } catch (Exception e) {
            System.out.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }
}
