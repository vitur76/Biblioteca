import java.io.*;
import java.sql.*;
import java.util.*;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Biblioteca {
    private static final String DB_URL = "jdbc:sqlite:biblioteca.db";
    private static final int PERIOADA_IMPRUMUT_ZILE = 14;

    public Biblioteca() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String createCartiTableSQL = "CREATE TABLE IF NOT EXISTS carti (" +
                    "titlu TEXT PRIMARY KEY, " +
                    "autor TEXT, " +
                    "gen TEXT, " +
                    "stoc INTEGER);";

            String createImprumuturiTableSQL = "CREATE TABLE IF NOT EXISTS imprumuturi (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "utilizator TEXT, " +
                    "titlu TEXT, " +
                    "data_imprumut DATE, " +
                    "data_returnare DATE, " +
                    "FOREIGN KEY (titlu) REFERENCES carti(titlu));";

            stmt.execute(createCartiTableSQL);
            stmt.execute(createImprumuturiTableSQL);
        } catch (SQLException e) {
            System.err.println("Eroare la initializarea bazei de date: " + e.getMessage());
        }
    }

    public void adaugaCarte(Carte carte) {
        String sql = "INSERT OR REPLACE INTO carti (titlu, autor, gen, stoc) VALUES (?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, carte.getTitlu());
            pstmt.setString(2, carte.getAutor());
            pstmt.setString(3, carte.getGen());
            pstmt.setInt(4, carte.getStoc());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Eroare la adaugarea cartii: " + e.getMessage());
        }
    }

}