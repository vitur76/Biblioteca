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

    public void stergeCarte(String titlu) {
        String sql = "DELETE FROM carti WHERE titlu = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titlu);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Eroare la stergerea cartii: " + e.getMessage());
        }
    }

    public void actualizeazaCarte(String titlu, int stocNou) {
        String sql = "UPDATE carti SET stoc = ? WHERE titlu = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, stocNou);
            pstmt.setString(2, titlu);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Eroare la actualizarea cartii: " + e.getMessage());
        }
    }

    public List<Carte> cautaCarti(String criteriu) {
        List<Carte> rezultate = new ArrayList<>();
        String sql = "SELECT * FROM carti WHERE titlu LIKE ? OR autor LIKE ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + criteriu + "%");
            pstmt.setString(2, "%" + criteriu + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String titlu = rs.getString("titlu");
                String autor = rs.getString("autor");
                String gen = rs.getString("gen");
                int stoc = rs.getInt("stoc");
                rezultate.add(new Carte(titlu, autor, gen, stoc));
            }
        } catch (SQLException e) {
            System.err.println("Eroare la cautarea cartilor: " + e.getMessage());
        }
        return rezultate;
    }

    public boolean imprumutaCarte(String utilizator, String titlu) {
        String sqlSelect = "SELECT stoc FROM carti WHERE titlu = ?;";
        String sqlInsertImprumut = "INSERT INTO imprumuturi (utilizator, titlu, data_imprumut, data_returnare) VALUES (?, ?, date('now'), date('now', '+' || ? || ' days'));";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect);
             PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertImprumut)) {

            pstmtSelect.setString(1, titlu);
            ResultSet rs = pstmtSelect.executeQuery();

            if (rs.next()) {
                int stoc = rs.getInt("stoc");
                if (stoc > 0) {
                    actualizeazaCarte(titlu, stoc - 1);

                    pstmtInsert.setString(1, utilizator);
                    pstmtInsert.setString(2, titlu);
                    pstmtInsert.setInt(3, PERIOADA_IMPRUMUT_ZILE);
                    pstmtInsert.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Eroare la imprumutarea cartii: " + e.getMessage());
        }
        return false;
    }

    public void returneazaCarte(String titlu) {
        String sqlSelect = "SELECT stoc FROM carti WHERE titlu = ?;";
        String sqlUpdateImprumut = "UPDATE imprumuturi SET data_returnare = date('now') WHERE titlu = ? AND data_returnare IS NULL;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect);
             PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateImprumut)) {

            pstmtSelect.setString(1, titlu);
            ResultSet rs = pstmtSelect.executeQuery();

            if (rs.next()) {
                int stoc = rs.getInt("stoc");
                actualizeazaCarte(titlu, stoc + 1);

                pstmtUpdate.setString(1, titlu);
                pstmtUpdate.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Eroare la returnarea cartii: " + e.getMessage());
        }
    }

    public void verificaImprumuturiExpirate() {
        String sql = "SELECT * FROM imprumuturi WHERE data_returnare < date('now') AND data_returnare IS NOT NULL;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Imprumuturi expirate:");
            while (rs.next()) {
                String utilizator = rs.getString("utilizator");
                String titlu = rs.getString("titlu");
                String dataImprumut = rs.getString("data_imprumut");
                String dataReturnare = rs.getString("data_returnare");

                System.out.println("Utilizator: " + utilizator + ", Titlu: " + titlu + ", Data Imprumut: " + dataImprumut + ", Data Returnare: " + dataReturnare);

                // Trimiterea notificării prin e-mail sau afișarea unui mesaj simplu
                trimitereNotificare(utilizator, titlu);
            }
        } catch (SQLException e) {
            System.err.println("Eroare la verificarea imprumuturilor expirate: " + e.getMessage());
        }
    }

    // Metoda pentru trimiterea notificării sau afișarea mesajului de întârziere
    private void trimitereNotificare(String utilizator, String titlu) {
        // Logic pentru trimiterea notificării
        System.out.println("Notificare: Cartea '" + titlu + "' nu a fost returnată la timp de utilizatorul " + utilizator + ".");
    }


    public void afiseazaIstoricImprumuturi(String utilizator) {
        String sql = "SELECT * FROM imprumuturi WHERE utilizator = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, utilizator);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String titlu = rs.getString("titlu");
                String dataImprumut = rs.getString("data_imprumut");
                String dataReturnare = rs.getString("data_returnare");
                System.out.println("Titlu: " + titlu + ", Data Imprumut: " + dataImprumut + ", Data Returnare: " + (dataReturnare != null ? dataReturnare : "Nereturnata"));
            }
        } catch (SQLException e) {
            System.err.println("Eroare la afisarea istoricului de imprumuturi: " + e.getMessage());
        }
    }

    public void afiseazaToate() {
        String sql = "SELECT * FROM carti;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String titlu = rs.getString("titlu");
                String autor = rs.getString("autor");
                String gen = rs.getString("gen");
                int stoc = rs.getInt("stoc");
                System.out.println(new Carte(titlu, autor, gen, stoc));
            }
        } catch (SQLException e) {
            System.err.println("Eroare la afisarea cartilor: " + e.getMessage());
        }
    }

}