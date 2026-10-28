package com.enigma;

import com.enigma.model.Case;
import com.enigma.model.Clue;
import com.enigma.model.Detective;
import com.enigma.model.Evidence;
import com.enigma.model.Location;
import com.enigma.model.Suspect;
import com.enigma.model.Victim;
import com.enigma.service.DeductionService;
import com.enigma.service.InterrogationService;
import com.enigma.service.InvestigationService;
import com.enigma.service.ScoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Main game loop: this is the simple text-based front end for the Java services. */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Suspect> suspects = new ArrayList<>();
    private static Detective detective;
    private static Case enigmaCase;
    private static InvestigationService investigationService;
    private static InterrogationService interrogationService;
    private static DeductionService deductionService;
    private static ScoreService scoreService;

    public static void main(String[] args) {
        setupGame();
        showBriefing();
        boolean running = true;
        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": investigateLocation(); break;
                case "2": interviewSuspect(); break;
                case "3": showEvidence(); break;
                case "4": deductionService.displayDeduction(suspects); break;
                case "5": running = makeAccusation(); break;
                case "0": running = false; System.out.println("Case paused. Your evidence is lost when the program closes."); break;
                default: System.out.println("Choose a number from the menu.");
            }
        }
        scanner.close();
    }

    private static void setupGame() {
        System.out.println("=====================================");
        System.out.println("       ENIGMA: THE LAST BROADCAST");
        System.out.println("=====================================");
        System.out.print("Detective, what is your name? ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "Detective";
        detective = new Detective(name, 0, "Not specified");

        enigmaCase = new Case("001", "The Last Broadcast",
                "TV producer Evan was found unconscious in locked Studio 7 after a three-minute blackout.");
        Victim victim = new Victim("Evan", 38, "Male", "Unknown",
                "Television Producer", "Studio 7", "Connected to all four suspects");
        enigmaCase.addVictim(victim);

        Location studio = new Location("Studio 7", "The locked studio where Evan was found.");
        Location security = new Location("Security Room", "The security systems, access logs and CCTV controls.");
        Location editing = new Location("Editing Room", "Evan's documentary recordings about Maya.");
        Location archive = new Location("Archive Room", "Old records from Maya's case five years ago.");
        enigmaCase.addLocation(studio); enigmaCase.addLocation(security);
        enigmaCase.addLocation(editing); enigmaCase.addLocation(archive);

        suspects.add(new Suspect("Noah", 35, "Male", "He feared Evan would reveal the old case.",
                "Says he was in the cafeteria.", "Evan's business partner", 35, false));
        suspects.add(new Suspect("Nora", 31, "Female", "She wanted to hide her connection to Maya.",
                "Says she stayed in her dressing room.", "Actress and Maya's friend", 40, false));
        suspects.add(new Suspect("Dean", 42, "Male", "He wanted to keep the security records hidden.",
                "Says the blackout was an electrical failure.", "Security manager", 70, true));
        suspects.add(new Suspect("Ivy", 27, "Female", "She wanted to protect Maya's memory.",
                "Says she barely knew Maya.", "Maya's younger sister", 30, false));
        for (Suspect suspect : suspects) enigmaCase.addSuspect(suspect);

        investigationService = new InvestigationService();
        interrogationService = new InterrogationService();
        deductionService = new DeductionService();
        scoreService = new ScoreService();
        addClues(studio, security, editing, archive);
    }

    private static void addClues(Location studio, Location security, Location editing, Location archive) {
        investigationService.addClue(new Clue(1, "Broken watch", "It stopped at 11:47 PM, the start of the blackout.", studio));
        investigationService.addClue(new Clue(2, "Two coffee cups", "A second person was likely with Evan before he was attacked.", studio));
        investigationService.addClue(new Clue(3, "Missing red USB", "Evan's laptop shows a USB was connected, but the device is gone.", studio));
        investigationService.addClue(new Clue(4, "Red fabric", "A torn red thread was caught on the Studio 7 door.", studio));
        investigationService.addClue(new Clue(5, "Security access log", "A security administration card was used at 11:48 PM.", security));
        investigationService.addClue(new Clue(6, "Manually deleted CCTV", "Only the blackout footage is missing; the deletion was deliberate.", security));
        investigationService.addClue(new Clue(7, "Dean's damaged uniform", "A piece is missing from Dean's red security uniform; it matches the Studio 7 fabric.", security));
        investigationService.addClue(new Clue(8, "Old security record", "Evan's files show that security records in Maya's case were altered five years ago.", editing));
        investigationService.addClue(new Clue(9, "Maya's diary page", "Maya warned Ivy not to trust the four people who were with her that night.", archive));
    }

    private static void showBriefing() {
        System.out.println("\nCASE " + enigmaCase.getCaseId() + " — " + enigmaCase.getTitle());
        System.out.println(enigmaCase.getDescription());
        System.out.println("At 11:47 PM the station lost power. Evan was found three minutes later.");
        System.out.println("Everyone may be hiding something. Collect evidence before accusing anyone.");
    }

    private static void showMenu() {
        System.out.println("\n--- Detective " + detective.getName() + " | Score: " + detective.getScore() + " ---");
        System.out.println("1. Investigate a location");
        System.out.println("2. Interview a suspect");
        System.out.println("3. Review collected evidence");
        System.out.println("4. Review deductions");
        System.out.println("5. Make final accusation");
        System.out.println("0. Quit");
        System.out.print("Choose: ");
    }

    private static void investigateLocation() {
        List<Location> locations = enigmaCase.getLocations();
        System.out.println("\nChoose a location:");
        for (int i = 0; i < locations.size(); i++) {
            System.out.println((i + 1) + ". " + locations.get(i).getName() + " — " + locations.get(i).getDescription());
        }
        System.out.print("Number (or 0 to return): ");
        int choice = readNumber();
        if (choice == 0) return;
        if (choice < 1 || choice > locations.size()) { System.out.println("That location is not on the list."); return; }
        Location location = locations.get(choice - 1);
        List<Clue> clues = investigationService.getCluesAtLocation(location);
        System.out.println("\n" + location.getName() + ": " + location.getDescription());
        if (clues.isEmpty()) { System.out.println("No clues are available here."); return; }
        for (Clue clue : clues) {
            System.out.println("\nCLUE: " + clue.getName());
            System.out.println(clue.getDescription());
            if (alreadyCollected(clue.getId())) {
                System.out.println("(Already added to your evidence.)");
            } else {
                Evidence evidence = investigationService.collectEvidence(clue);
                detective.collectEvidence(evidence);
                enigmaCase.addClue(clue);
                detective.addScore(10);
                System.out.println("Added to evidence. +10 points.");
                if (clue.getId() == 5 || clue.getId() == 6 || clue.getId() == 7 || clue.getId() == 8) {
                    updateSuspicion("Dean", 10);
                }
            }
        }
    }

    private static boolean alreadyCollected(int clueId) {
        for (Evidence evidence : detective.getCollectedEvidence()) if (evidence.getId() == clueId) return true;
        return false;
    }

    private static void interviewSuspect() {
        System.out.println("\nWho would you like to question?");
        for (int i = 0; i < suspects.size(); i++) System.out.println((i + 1) + ". " + suspects.get(i).getName());
        System.out.print("Number (or 0 to return): ");
        int choice = readNumber();
        if (choice == 0) return;
        if (choice < 1 || choice > suspects.size()) { System.out.println("No suspect has that number."); return; }
        Suspect suspect = suspects.get(choice - 1);
        interrogationService.interrogate(suspect, scanner, alreadyCollected(5) || alreadyCollected(6));
        scoreService.awardInterrogationPoints(detective);
    }

    private static void showEvidence() {
        System.out.println("\n--- Evidence collected: " + detective.getCollectedEvidence().size() + " ---");
        if (detective.getCollectedEvidence().isEmpty()) { System.out.println("You have not collected any evidence yet."); return; }
        for (Evidence evidence : detective.getCollectedEvidence()) {
            System.out.println("* " + evidence.getName() + " — " + evidence.getDescription());
        }
    }

    private static boolean makeAccusation() {
        System.out.println("\nWho attacked Evan?");
        for (int i = 0; i < suspects.size(); i++) System.out.println((i + 1) + ". " + suspects.get(i).getName());
        System.out.print("Number (or 0 to return): ");
        int choice = readNumber();
        if (choice == 0) return true;
        if (choice < 1 || choice > suspects.size()) { System.out.println("Choose a suspect from the list."); return true; }
        Suspect selected = suspects.get(choice - 1);
        if (deductionService.checkCulprit(selected) && hasRequiredEvidence()) {
            scoreService.awardCorrectDeduction(detective);
            System.out.println("\nCASE SOLVED — Dean planned the blackout, used his security access, attacked Evan, took the red USB and deleted the CCTV.");
            System.out.println("He wanted to hide his part in changing the records from Maya's case five years ago.");
            System.out.println("Final score: " + detective.getScore());
            return false;
        }
        if (deductionService.checkCulprit(selected)) {
            System.out.println("You suspect the right person, but need more evidence before you can prove it.");
        } else {
            System.out.println("That accusation does not fit what you know. Keep investigating.");
            scoreService.deductPoints(detective, 5);
        }
        return true;
    }

    private static boolean hasRequiredEvidence() {
        return alreadyCollected(3) && alreadyCollected(4) && alreadyCollected(5)
                && alreadyCollected(6) && alreadyCollected(7) && alreadyCollected(8);
    }

    private static void updateSuspicion(String name, int points) {
        for (Suspect suspect : suspects) if (suspect.getName().equals(name)) {
            suspect.setSuspicionLevel(Math.min(100, suspect.getSuspicionLevel() + points));
        }
    }

    private static int readNumber() {
        String input = scanner.nextLine().trim();
        try { return Integer.parseInt(input); }
        catch (NumberFormatException exception) { return -1; }
    }
}
