import java.io.*;
import java.util.*;

class Carte {
    private String titlu;
    private String autor;
    private String gen;
    private int stoc;

    public Carte(String titlu, String autor, String gen, int stoc) {
        this.titlu = titlu;
        this.autor = autor;
        this.gen = gen;
        this.stoc = stoc;
    }

    public String getTitlu() {
        return titlu;
    }

    public String getAutor() {
        return autor;
    }

    public String getGen() {
        return gen;
    }

    public int getStoc() {
        return stoc;
    }

    public void setStoc(int stoc) {
        this.stoc = stoc;
    }

    @Override
    public String toString() {
        return "Carte{" +
                "titlu='" + titlu + '\'' +
                ", autor='" + autor + '\'' +
                ", gen='" + gen + '\'' +
                ", stoc=" + stoc +
                '}';
    }
}