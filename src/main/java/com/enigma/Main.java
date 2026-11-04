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

/** Starts the console game and handles the player's choices. */
public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final InvestigationService investigationService = new InvestigationService();
    private final InterrogationService interrogationService = new InterrogationService(scanner);
    private final DeductionService deductionService = new DeductionService();
    private final ScoreService scoreService = new ScoreService();

    // ArrayLists remember which clues and suspects have already been handled.
    private final ArrayList<Integer> collectedClueIds = new ArrayList<>();
    private final ArrayList<String> interviewedSuspects = new ArrayList<>();

    private Detective detective;
    private Case enigmaCase;
    private boolean caseSolved;

    public static void main(String[] args) {
        Main game = new Main();
        game.start();
    }

    public void start() {
        printTitle();
        createDetective();
        createCase();
        showBriefing();

        boolean playing = true;
        while (playing && !caseSolved) {
            showMenu();
            int choice = readNumber("Choose an option: ", 1, 7);

            switch (choice) {
                case 1:
                    investigateLocation();
                    break;
                case 2:
                    interviewSuspect();
                    break;
                case 3:
                    showEvidence();
                    break;
                case 4:
                    deductionService.displayDeduction(enigmaCase.getSuspects());
                    break;
                case 5:
                    makeAccusation();
                    break;
                case 6:
                    scoreService.displayScore(detective);
                    break;
                case 7:
                    playing = false;
                    break;
                default:
                    System.out.println("Please choose a number from 1 to 7.");
            }

            if (playing && !caseSolved) {
                pause();
            }
        }

        showFinalReport();
        scanner.close();
    }

    private void printTitle() {
        System.out.println("======================================");
        System.out.println("                 ENIGMA");
        System.out.println("======================================");
    }

    private void createDetective() {
        System.out.println("\nWelcome, Detective. Enter your details to open the case.");
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = "Detective";
        }

        int age = readNumber("Age: ", 1, 130);
        System.out.print("Gender: ");
        String gender = scanner.nextLine().trim();
        if (gender.isEmpty()) {
            gender = "Not specified";
        }

        detective = new Detective(name, age, gender);
    }

    private void createCase() {
        enigmaCase = new Case("011", "The Last Broadcast",
                "Producer Evan disappeared from Studio 7 during a three-minute blackout. "
                        + "Search the station and question the people connected to him to uncover the truth.");

        Victim victim = new Victim("Evan", 38, "Male", "Unknown",
                "Television Producer", "Studio 7", "Connected to all four suspects");
        enigmaCase.addVictim(victim);

        Location studio = new Location("Studio 7",
                "The locked broadcasting studio where Evan disappeared.");
        Location securityRoom = new Location("Security Room",
                "The room containing security footage and access records.");
        Location editingRoom = new Location("Editing Room",
                "The room containing old recordings and audio evidence.");
        Location archiveRoom = new Location("Archive Room",
                "The room containing old case files and Maya's belongings.");

        enigmaCase.addLocation(studio);
        enigmaCase.addLocation(securityRoom);
        enigmaCase.addLocation(editingRoom);
        enigmaCase.addLocation(archiveRoom);

        addSuspect("Noah", 35, "Male", "Wanted to stop Evan from revealing the old case.",
                "Claims he was in the cafeteria.", "Evan's business partner", 35, false);
        addSuspect("Nora", 31, "Female", "Wanted to keep her connection to Maya secret.",
                "Claims she stayed in her dressing room.", "Friend of Maya", 40, false);
        addSuspect("Dean", 42, "Male", "Wanted to prevent the original evidence from exposing him.",
                "Claims he was checking the security system.", "Security manager", 70, true);
        addSuspect("Ivy", 27, "Female", "Wanted to protect her family and Maya's secrets.",
                "Claims she was in the station lobby.", "Maya's younger sister", 30, false);

        addClue(1, "Broken watch", "It stopped at 11:47 PM, when the blackout began.", studio);
        addClue(2, "Red thread", "A piece of red thread is caught on the Studio 7 door.", studio);
        addClue(3, "Security access log", "An administration card was used at 11:48 PM.", securityRoom);
        addClue(4, "Deleted CCTV footage", "The three-minute blackout segment was manually deleted.", securityRoom);
        addClue(5, "Evan's audio recording", "Evan was reviewing the old case involving Maya before he disappeared.", editingRoom);
        addClue(6, "Altered archive file", "A security record from Maya's case was changed five years ago.", archiveRoom);
    }

    private void addSuspect(String name, int age, String gender, String motive,
                            String alibi, String relationship, int suspicion, boolean guilty) {
        Suspect suspect = new Suspect(name, age, gender, motive, alibi,
                relationship, suspicion, guilty);
        enigmaCase.addSuspect(suspect);
    }

    private void addClue(int id, String name, String description, Location location) {
        Clue clue = new Clue(id, name, description, location);
        investigationService.addClue(clue);
    }

    private void showBriefing() {
        Victim victim = enigmaCase.getVictims().get(0);
        System.out.println("\nCASE " + enigmaCase.getCaseId() + " — " + enigmaCase.getTitle());
        System.out.println(enigmaCase.getDescription());
        System.out.println("\nMissing person: " + victim.getName());
        System.out.println("Last seen: " + victim.getLastSeenLocation());
        System.out.println("\nSearch locations and interview suspects to collect evidence.");
        pause();
    }

    private void showMenu() {
        System.out.println("\n--------------------------------------");
        System.out.println("Detective " + detective.getName() + " | Score: " + detective.getScore());
        System.out.println("1. Investigate a location");
        System.out.println("2. Interview a suspect");
        System.out.println("3. View collected evidence");
        System.out.println("4. View deductions");
        System.out.println("5. Make an accusation");
        System.out.println("6. View detective profile");
        System.out.println("7. Finish investigation");
    }

    private void investigateLocation() {
        List<Location> locations = enigmaCase.getLocations();
        System.out.println("\nChoose a location to search:");
        for (int i = 0; i < locations.size(); i++) {
            System.out.println((i + 1) + ". " + locations.get(i).getName());
        }

        int choice = readNumber("Location: ", 1, locations.size());
        Location selectedLocation = locations.get(choice - 1);
        List<Clue> clues = investigationService.getCluesAtLocation(selectedLocation);
        boolean foundNewClue = false;

        System.out.println("\nYou search " + selectedLocation.getName() + ".");
        for (Clue clue : clues) {
            if (!collectedClueIds.contains(clue.getId())) {
                collectedClueIds.add(clue.getId());
                Evidence evidence = investigationService.collectEvidence(clue);
                detective.collectEvidence(evidence);
                enigmaCase.addClue(clue);

                System.out.println("\nNEW CLUE: " + clue.getName());
                System.out.println(clue.getDescription());
                scoreService.awardCluePoints(detective);
                foundNewClue = true;
            }
        }

        if (!foundNewClue) {
            System.out.println("You already collected all the clues from this location.");
        }
    }

    private void interviewSuspect() {
        Suspect suspect = chooseSuspect();
        if (suspect == null) {
            return;
        }

        interrogationService.interrogate(suspect);
        if (!interviewedSuspects.contains(suspect.getName())) {
            interviewedSuspects.add(suspect.getName());
            scoreService.awardInterrogationPoints(detective);
        }
    }

    private Suspect chooseSuspect() {
        List<Suspect> suspects = enigmaCase.getSuspects();
        System.out.println("\nChoose a suspect:");
        for (int i = 0; i < suspects.size(); i++) {
            System.out.println((i + 1) + ". " + suspects.get(i).getName());
        }

        int choice = readNumber("Suspect: ", 1, suspects.size());
        return suspects.get(choice - 1);
    }

    private void showEvidence() {
        List<Evidence> evidenceList = detective.getCollectedEvidence();
        System.out.println("\nCOLLECTED EVIDENCE");
        if (evidenceList.isEmpty()) {
            System.out.println("No evidence yet. Investigate a location first.");
            return;
        }

        for (Evidence evidence : evidenceList) {
            System.out.println("\n" + evidence.getId() + ". " + evidence.getName());
            System.out.println(evidence.getDescription());
        }
    }

    private void makeAccusation() {
        if (!collectedClueIds.contains(3)
                || !collectedClueIds.contains(4)
                || !collectedClueIds.contains(6)) {
            System.out.println("\nFirst collect the security access log, deleted CCTV footage, "
                    + "and altered archive file.");
            return;
        }

        Suspect accused = chooseSuspect();
        if (accused == null) {
            return;
        }

        System.out.println("\n" + deductionService.getDeductionMessage(accused));
        if (deductionService.checkCulprit(accused)) {
            caseSolved = true;
            scoreService.awardCorrectDeduction(detective);
            System.out.println("\nCASE SOLVED: " + accused.getName() + " was responsible.");
            System.out.println("The security access, deleted footage, and altered Maya case record are connected.");
        } else {
            scoreService.deductPoints(detective, 5);
            System.out.println("That accusation is incorrect. You can keep investigating.");
        }
    }

    private void showFinalReport() {
        System.out.println("\n======================================");
        System.out.println("              CASE REPORT");
        System.out.println("======================================");
        System.out.println("Case: " + enigmaCase.getTitle());
        System.out.println("Detective: " + detective.getName());
        System.out.println("Status: " + (caseSolved ? "SOLVED" : "UNRESOLVED"));
        System.out.println("Evidence collected: " + detective.getCollectedEvidence().size());
        System.out.println("Final score: " + detective.getScore());
    }

    private int readNumber(String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            String answer = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(answer);
                if (number >= minimum && number <= maximum) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
                // Ask again when the input is not a valid number.
            }
            System.out.println("Enter a number from " + minimum + " to " + maximum + ".");
        }
    }

    private void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
