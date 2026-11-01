package com.enigma;

import com.enigma.model.Case;
import com.enigma.model.Clue;
import com.enigma.model.Detective;
import com.enigma.model.Evidence;
import com.enigma.model.Location;
import com.enigma.model.Question;
import com.enigma.model.Suspect;
import com.enigma.model.Victim;
import com.enigma.service.DeductionService;
import com.enigma.service.InterrogationService;
import com.enigma.service.InvestigationService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Holds one player's case state and connects the game UI/API to the services. */
public class GameController {
    private final Detective detective;
    private final Case gameCase;
    private final InvestigationService investigationService;
    private final InterrogationService interrogationService;
    private final DeductionService deductionService;
    private final List<Suspect> suspects = new ArrayList<>();
    private final Set<Integer> collectedClueIds = new HashSet<>();
    private final Set<String> investigatedLocations = new HashSet<>();
    private final Set<String> interviewedSuspects = new HashSet<>();
    private final Set<String> askedQuestionKeys = new HashSet<>();
    private int interrogationsCompleted;
    private boolean solved;

    public GameController(String detectiveName) { this(detectiveName, 0, "Not specified"); }

    public GameController(String detectiveName, int age, String gender) {
        String safeName = detectiveName == null || detectiveName.trim().isEmpty() ? "Detective" : detectiveName.trim();
        String safeGender = gender == null || gender.trim().isEmpty() ? "Not specified" : gender.trim();
        detective = new Detective(safeName, Math.max(0, age), safeGender);
        gameCase = new Case("011", "The Last Broadcast",
                "TV producer Evan was found unconscious in locked Studio 7 after a three-minute blackout.");
        investigationService = new InvestigationService();
        interrogationService = new InterrogationService();
        deductionService = new DeductionService();
        createCaseData();
    }

    private void createCaseData() {
        gameCase.addVictim(new Victim("Evan", 38, "Male", "Unknown", "Television Producer",
                "Studio 7", "Connected to all four suspects"));

        Location studio = new Location("Studio 7", "The locked studio where Evan was found.");
        Location security = new Location("Security Room", "The security systems, access logs and CCTV controls.");
        Location editing = new Location("Editing Room", "Evan's documentary recordings about Maya.");
        Location archive = new Location("Archive Room", "Old records from Maya's case five years ago.");
        gameCase.addLocation(studio);
        gameCase.addLocation(security);
        gameCase.addLocation(editing);
        gameCase.addLocation(archive);

        addSuspect(new Suspect("Noah", 35, "Male", "He feared Evan would reveal the old case.",
                "Says he was in the cafeteria.", "Evan's business partner", 35, false));
        addSuspect(new Suspect("Nora", 31, "Female", "She wanted to hide her connection to Maya.",
                "Says she stayed in her dressing room.", "Actress and Maya's friend", 40, false));
        addSuspect(new Suspect("Dean", 42, "Male", "He wanted to keep the security records hidden.",
                "Says the blackout was an electrical failure.", "Security manager", 70, true));
        addSuspect(new Suspect("Ivy", 27, "Female", "She wanted to protect Maya's memory.",
                "Says she barely knew Maya.", "Maya's younger sister", 30, false));

        addClue(1, "Broken watch", "It stopped at 11:47 PM, the start of the blackout.", studio);
        addClue(2, "Two coffee cups", "A second person was likely with Evan before he was attacked.", studio);
        addClue(3, "Missing red USB", "Evan's laptop shows a USB was connected, but the device is gone.", studio);
        addClue(4, "Red fabric", "A torn red thread was caught on the Studio 7 door.", studio);
        addClue(10, "Coffee cup fingerprint", "A fingerprint on one cup matches Nora.", studio);
        addClue(5, "Security access log", "A security administration card was used at 11:48 PM.", security);
        addClue(6, "Manually deleted CCTV", "Only the blackout footage is missing; the deletion was deliberate.", security);
        addClue(7, "Dean's damaged uniform", "A piece is missing from Dean's red uniform; it matches the Studio 7 fabric.", security);
        addClue(11, "Cafeteria CCTV", "Noah does not appear in the cafeteria footage during the blackout.", security);
        addClue(8, "Old security record", "Security records in Maya's case were altered five years ago.", editing);
        addClue(9, "Maya's diary page", "Maya warned Ivy not to trust the four people who were with her that night.", archive);
    }

    private void addSuspect(Suspect suspect) {
        suspects.add(suspect);
        gameCase.addSuspect(suspect);
    }

    private void addClue(int id, String name, String description, Location location) {
        investigationService.addClue(new Clue(id, name, description, location));
    }

    public List<String> getLocationNames() {
        List<String> names = new ArrayList<>();
        for (Location location : gameCase.getLocations()) names.add(location.getName());
        return names;
    }

    public List<String> getSuspectNames() {
        List<String> names = new ArrayList<>();
        for (Suspect suspect : suspects) names.add(suspect.getName());
        return names;
    }

    public List<Location> getLocations() { return new ArrayList<>(gameCase.getLocations()); }
    public List<Suspect> getSuspects() { return new ArrayList<>(suspects); }
    public List<Victim> getVictims() { return new ArrayList<>(gameCase.getVictims()); }
    public List<Evidence> getCollectedEvidence() { return new ArrayList<>(detective.getCollectedEvidence()); }
    public String getDetectiveName() { return detective.getName(); }
    public int getDetectiveAge() { return detective.getAge(); }
    public String getDetectiveGender() { return detective.getGender(); }
    public String getCaseId() { return gameCase.getCaseId(); }
    public String getCaseTitle() { return gameCase.getTitle(); }
    public String getCaseDescription() { return gameCase.getDescription(); }
    public int getInterrogationsCompleted() { return interrogationsCompleted; }
    public int getScore() { return detective.getScore(); }
    public boolean isSolved() { return solved; }

    public boolean hasInterviewed(String suspectName) { return interviewedSuspects.contains(suspectName); }
    public boolean isLocationInvestigated(String name) { return investigatedLocations.contains(name); }
    public boolean hasEvidence(int evidenceId) { return collectedClueIds.contains(evidenceId); }

    public List<Question> getInterviewQuestions(String suspectName) {
        Suspect suspect = findSuspect(suspectName);
        return suspect == null ? List.of() : interrogationService.getQuestions(suspect);
    }

    public String getObjective() {
        if (solved) return "Case solved — review the final case report.";
        if (collectedClueIds.isEmpty()) return "Search Studio 7 for the first physical clues.";
        if (!collectedClueIds.contains(5) || !collectedClueIds.contains(6)) return "Check the Security Room logs and missing CCTV.";
        if (!collectedClueIds.contains(8)) return "Find Evan's old security record in the Editing Room.";
        if (!hasRequiredEvidence(collectedClueIds)) return "Connect the evidence needed to support your theory.";
        return "Build your theory on the deduction board and name the person responsible.";
    }
    public String getBriefing() {
        return "CASE " + gameCase.getCaseId() + " — " + gameCase.getTitle() + "\n\n"
                + gameCase.getDescription() + "\n\n"
                + "At 11:47 PM the station lost power. Evan was found three minutes later.\n"
                + "Investigate locations, question suspects, and collect evidence before accusing anyone.";
    }

    public String investigate(String locationName) {
        Location selected = findLocation(locationName);
        if (selected == null) return "That location is not available in this case.";
        investigatedLocations.add(selected.getName());
        StringBuilder result = new StringBuilder("You investigate ").append(selected.getName()).append(".\n");
        List<Clue> clues = investigationService.getCluesAtLocation(selected);
        if (clues.isEmpty()) return result.append("No clues are available here.").toString();

        for (Clue clue : clues) {
            result.append("\nCLUE: ").append(clue.getName()).append("\n").append(clue.getDescription()).append("\n");
            if (collectedClueIds.add(clue.getId())) {
                detective.collectEvidence(investigationService.collectEvidence(clue));
                gameCase.addClue(clue);
                detective.addScore(10);
                result.append("Added to evidence (+10 points).\n");
                if (clue.getId() >= 5 && clue.getId() <= 8) increaseDeanSuspicion(10);
                if (clue.getId() == 10) increaseNoraSuspicion(10);
                if (clue.getId() == 11) increaseNoahSuspicion(10);
            } else {
                result.append("Already in your evidence.\n");
            }
        }
        return result.toString();
    }

    /** Compatibility method for the Swing game; asks the first available question. */
    public String interview(String suspectName) {
        List<Question> questions = getInterviewQuestions(suspectName);
        return questions.isEmpty() ? "That person is not listed as a suspect."
                : interview(suspectName, questions.get(0).getId());
    }

    public String interview(String suspectName, String questionId) {
        Suspect suspect = findSuspect(suspectName);
        if (suspect == null) return "That person is not listed as a suspect.";
        Question selected = null;
        for (Question question : interrogationService.getQuestions(suspect)) {
            if (question.getId().equals(questionId)) selected = question;
        }
        if (selected == null) return "Choose one of the questions shown for this suspect.";

        boolean newQuestion = askedQuestionKeys.add(suspectName + ":" + questionId);
        boolean firstInterview = interviewedSuspects.add(suspectName);
        if (firstInterview) interrogationsCompleted++;
        boolean hasCafeteria = collectedClueIds.contains(11);
        boolean hasFingerprint = collectedClueIds.contains(10);
        boolean hasSecurity = collectedClueIds.contains(5) || collectedClueIds.contains(6);
        boolean hasDiary = collectedClueIds.contains(9);
        String answer = interrogationService.askQuestion(suspect, questionId,
                hasCafeteria, hasFingerprint, hasSecurity, hasDiary);
        if (newQuestion) detective.addScore(5);
        return "DETECTIVE: " + selected.getQuestionText() + "\n\n" + answer
                + (newQuestion ? "\n\nNew statement recorded (+5 points)." : "\n\nYou already asked this question.");
    }

    public String presentEvidence(String suspectName, int evidenceId) {
        Suspect suspect = findSuspect(suspectName);
        if (suspect == null) return "Select a suspect before presenting evidence.";
        if (!collectedClueIds.contains(evidenceId)) return "You have not collected that evidence yet.";
        Evidence evidence = findEvidence(evidenceId);
        if (evidence == null) return "That evidence is not available in this case.";
        return "PRESENTED: " + evidence.getName() + "\n\n"
                + interrogationService.presentEvidence(suspect, evidenceId);
    }

    public String getEvidenceList() {
        StringBuilder result = new StringBuilder("Evidence collected: ")
                .append(detective.getCollectedEvidence().size()).append("\n\n");
        if (detective.getCollectedEvidence().isEmpty()) return result.append("No evidence yet.").toString();
        for (Evidence evidence : detective.getCollectedEvidence()) {
            result.append("• ").append(evidence.getName()).append("\n  ")
                    .append(evidence.getDescription()).append("\n\n");
        }
        return result.toString();
    }

    public String getDeductions() {
        StringBuilder result = new StringBuilder("Suspicion levels\n\n");
        for (Suspect suspect : suspects) {
            result.append(suspect.getName()).append(" — ").append(suspect.getSuspicionLevel()).append("/100\n");
        }
        Suspect mostSuspicious = deductionService.findMostSuspicious(suspects);
        if (mostSuspicious != null) result.append("\nCurrently most suspicious: ").append(mostSuspicious.getName());
        return result.toString();
    }

    public String accuse(String suspectName) { return accuse(suspectName, new ArrayList<>(collectedClueIds)); }

    public String accuse(String suspectName, List<Integer> evidenceIds) {
        if (solved) return "The case is already solved.";
        Suspect selected = findSuspect(suspectName);
        if (selected == null) return "Choose a listed suspect.";
        Set<Integer> ownedIds = new HashSet<>();
        if (evidenceIds != null) {
            for (Integer id : evidenceIds) if (id != null && collectedClueIds.contains(id)) ownedIds.add(id);
        }
        if (!deductionService.validateTheory(selected, ownedIds)) {
            if (!deductionService.hasRequiredEvidence(ownedIds)) {
                return "Your theory needs more evidence. Link the USB, fabric, security log, deleted CCTV, uniform match, and old security record.";
            }
            detective.addScore(-5);
            return "That suspect does not fit the evidence. Reconsider your theory. (-5 points)";
        }
        solved = true;
        detective.addScore(50);
        return "CASE SOLVED\n\nDean planned the blackout, used his security access, attacked Evan, took the red USB, and deleted the CCTV. "
                + "He wanted to hide his part in changing the records from Maya's case five years ago.\n\n"
                + "You earned 50 points. Final score: " + detective.getScore();
    }

    public String getReportSummary() {
        if (!solved) return "The case remains unresolved. Continue investigating before submitting a final report.";
        return "Dean planned the blackout, attacked Evan, took the red USB and deleted the CCTV to hide his involvement in altering the security records from Maya's case.";
    }

    public String getCulpritName() {
        if (!solved) return null;
        for (Suspect suspect : suspects) if (suspect.isGuilty()) return suspect.getName();
        return null;
    }

    private boolean hasRequiredEvidence(Set<Integer> evidenceIds) {
        return deductionService.hasRequiredEvidence(evidenceIds);
    }

    private Evidence findEvidence(int id) {
        for (Evidence evidence : detective.getCollectedEvidence()) if (evidence.getId() == id) return evidence;
        return null;
    }

    private Location findLocation(String name) {
        for (Location location : gameCase.getLocations()) if (location.getName().equals(name)) return location;
        return null;
    }

    private Suspect findSuspect(String name) {
        for (Suspect suspect : suspects) if (suspect.getName().equals(name)) return suspect;
        return null;
    }

    private void increaseSuspicion(String name, int points) {
        Suspect suspect = findSuspect(name);
        if (suspect != null) suspect.setSuspicionLevel(Math.min(100, suspect.getSuspicionLevel() + points));
    }
    private void increaseDeanSuspicion(int points) { increaseSuspicion("Dean", points); }
    private void increaseNoraSuspicion(int points) { increaseSuspicion("Nora", points); }
    private void increaseNoahSuspicion(int points) { increaseSuspicion("Noah", points); }
}

