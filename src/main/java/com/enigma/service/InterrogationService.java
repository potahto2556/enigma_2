package com.enigma.service;

import com.enigma.model.Suspect;

import java.util.Scanner;

public class InterrogationService {

    private final Scanner scanner;

    public InterrogationService(Scanner scanner) {
        this.scanner = scanner;
    }

    // Let the player choose a question and show the suspect's answer.
    public void interrogate(Suspect suspect) {
        System.out.println("\n===== INTERROGATION =====");
        System.out.println("Suspect: " + suspect.getName());
        System.out.println("Relationship to Evan: " + suspect.getRelationshipToVictim());

        boolean interviewing = true;
        boolean suspicionRaised = false;
        while (interviewing) {
            System.out.println("\nChoose a question:");
            System.out.println("1. Where were you at 11:47 PM?");
            System.out.println("2. What was your relationship with Evan?");
            System.out.println("3. What do you know about Maya's old case?");
            System.out.println("4. End the interview");

            int choice = readChoice();
            if (choice == 4) {
                interviewing = false;
            } else {
                System.out.println("\nDetective: " + getQuestion(choice));
                System.out.println(suspect.getName() + ": " + getAnswer(suspect, choice));

                // Dean's answer about the deleted footage makes him more suspicious.
                if (!suspicionRaised && "Dean".equals(suspect.getName()) && choice == 3) {
                    suspect.setSuspicionLevel(Math.min(100, suspect.getSuspicionLevel() + 10));
                    suspicionRaised = true;
                }
            }
        }

        System.out.println("\n===== INTERROGATION COMPLETE =====");
        System.out.println("Current Suspicion Level: " + suspect.getSuspicionLevel());
    }

    private String getQuestion(int choice) {
        if (choice == 1) return "Where were you at 11:47 PM?";
        if (choice == 2) return "What was your relationship with Evan?";
        return "What do you know about Maya's old case?";
    }

    private String getAnswer(Suspect suspect, int choice) {
        String name = suspect.getName();

        if ("Noah".equals(name)) {
            if (choice == 1) return "I was in the cafeteria when the power went out.";
            if (choice == 2) return "Evan was my business partner. We argued about the old case.";
            return "I knew Maya, but I did not want the old case reopened.";
        }
        if ("Nora".equals(name)) {
            if (choice == 1) return "I met Evan earlier, but I left before he disappeared.";
            if (choice == 2) return "Maya was my friend. Evan asked me about her.";
            return "I was afraid people would blame me, so I kept quiet.";
        }
        if ("Dean".equals(name)) {
            if (choice == 1) return "I was checking the security system.";
            if (choice == 2) return "I was responsible for security at the station.";
            return "I cannot explain why that old security record was changed.";
        }

        if (choice == 1) return "I was in the station lobby.";
        if (choice == 2) return "Maya was my sister. I wanted to protect her.";
        return "Maya warned me not to trust the people who were with her that night.";
    }

    private int readChoice() {
        while (true) {
            System.out.print("Question number: ");
            String answer = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(answer);
                if (choice >= 1 && choice <= 4) return choice;
            } catch (NumberFormatException ignored) {
                // Ask again if the input was not a number.
            }
            System.out.println("Enter a number from 1 to 4.");
        }
    }

    // Display suspect information before interrogation
    public void showSuspect(Suspect suspect) {

        System.out.println("\n----- SUSPECT -----");
        System.out.println("Name: " + suspect.getName());
        System.out.println("Age: " + suspect.getAge());
        System.out.println("Gender: " + suspect.getGender());
        System.out.println("Motive: " + suspect.getMotive());
        System.out.println("Alibi: " + suspect.getAlibi());
        System.out.println("Suspicion Level: "
                + suspect.getSuspicionLevel());
    }
}
