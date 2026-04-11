package org.example;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ServicePersonne service = new ServicePersonne();

        while (true) {
            System.out.println("\n--- CRUD Menu ---");
            System.out.println("1. Add Personne");
            System.out.println("2. Update Prenom");
            System.out.println("3. Delete Personne");
            System.out.println("4. Display All Personnes");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter nom: ");
                        String nom = scanner.nextLine();
                        System.out.print("Enter prenom: ");
                        String prenom = scanner.nextLine();
                        Personne newPersonne = new Personne(nom, prenom);
                        service.ajouter(newPersonne);
                        break;
                    case 2:
                        System.out.print("Enter id: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Enter new prenom: ");
                        String newPrenom = scanner.nextLine();
                        Personne updatePersonne = new Personne(id, null, newPrenom);
                        service.updatePrenom(updatePersonne);
                        break;
                    case 3:
                        System.out.print("Enter id to delete: ");
                        int deleteId = scanner.nextInt();
                        service.delete(deleteId);
                        break;
                    case 4:
                        ArrayList<Personne> list = service.afficherAll();
                        for (Personne p : list) {
                            System.out.println(p);
                        }
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}