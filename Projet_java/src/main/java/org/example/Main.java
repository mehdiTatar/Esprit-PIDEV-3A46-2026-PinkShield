package org.example;

import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Démarrage des tests CRUD ---");
        ServicePersonne service = new ServicePersonne();

        try {
            Personne personneAModifier = new Personne(1, "Tounsi", "Ali");
            service.updatePrenom(personneAModifier);

            Personne fauxProfil = new Personne("Test", "A_Supprimer");
            service.ajouter(fauxProfil);

            service.delete(2);

            System.out.println("\n--- Résultat final dans la base de données ---");
            ArrayList<Personne> liste = service.afficherAll();
            for (Personne p : liste) {
                System.out.println(p.toString());
            }

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}