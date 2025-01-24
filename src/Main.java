import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Biblioteca biblioteca = new Biblioteca();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nMeniu Biblioteca:");
            System.out.println("1. Adauga carte");
            System.out.println("2. Sterge carte");
            System.out.println("3. Cauta carti");
            System.out.println("4. Imprumuta carte");
            System.out.println("5. Returneaza carte");
            System.out.println("6. Afiseaza toate cartile");
            System.out.println("7. Verifica imprumuturi expirate");
            System.out.println("8. Istoric imprumuturi utilizator");
            System.out.println("9. Iesire");
            System.out.print("Selectati o optiune: ");

            int optiune = scanner.nextInt();
            scanner.nextLine(); // Consumam newline

            switch (optiune) {
                case 1:
                    System.out.print("Titlu: ");
                    String titlu = scanner.nextLine();
                    System.out.print("Autor: ");
                    String autor = scanner.nextLine();
                    System.out.print("Gen: ");
                    String gen = scanner.nextLine();
                    System.out.print("Stoc: ");
                    int stoc = scanner.nextInt();
                    biblioteca.adaugaCarte(new Carte(titlu, autor, gen, stoc));
                    break;
                case 2:
                    System.out.print("Titlu: ");
                    titlu = scanner.nextLine();
                    biblioteca.stergeCarte(titlu);
                    break;
                case 3:
                    System.out.print("Criteriu cautare: ");
                    String criteriu = scanner.nextLine();
                    List<Carte> rezultate = biblioteca.cautaCarti(criteriu);
                    for (Carte carte : rezultate) {
                        System.out.println(carte);
                    }
                    break;
                case 4:
                    System.out.print("Utilizator: ");
                    String utilizator = scanner.nextLine();
                    System.out.print("Titlu: ");
                    titlu = scanner.nextLine();
                    if (biblioteca.imprumutaCarte(utilizator, titlu)) {
                        System.out.println("Cartea a fost imprumutata cu succes!");
                    } else {
                        System.out.println("Cartea nu este disponibila!");
                    }
                    break;
                case 5:
                    System.out.print("Titlu: ");
                    titlu = scanner.nextLine();
                    biblioteca.returneazaCarte(titlu);
                    break;
                case 6:
                    biblioteca.afiseazaToate();
                    break;
                case 7:
                    biblioteca.verificaImprumuturiExpirate();
                    break;
                case 8:
                    System.out.print("Utilizator: ");
                    utilizator = scanner.nextLine();
                    biblioteca.afiseazaIstoricImprumuturi(utilizator);
                    break;
                case 9:
                    System.out.println("Iesire...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Optiune invalida!");
            }
        }
    }
}