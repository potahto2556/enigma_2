package com.enigma.api;

import java.util.List;

/** JSON shapes exchanged between the browser and Java API. */
public final class ApiDtos {
    private ApiDtos() { }
    public record CreateGameRequest(String name, Integer age, String gender) { }
    public record DetectiveProfile(String name, Integer age, String gender) { }
    public record CaseFile(String caseId, String title, String description, String victimName,
                           int victimAge, String victimOccupation, String lastSeenLocation) { }
    public record GameStarted(String gameId, DetectiveProfile detective, CaseFile caseFile,
                              List<String> locations, List<String> suspects) { }
    public record DashboardView(String caseId, String caseTitle, String detectiveName, int score,
                                int evidenceCollected, int totalEvidence, int locationsInvestigated,
                                int totalLocations, int interrogationsCompleted, String objective) { }
    public record LocationView(String name, String description, boolean investigated) { }
    public record SuspectView(String name, int age, String gender, String relationship,
                              String motive, String alibi, int suspicionLevel, boolean interviewed) { }
    public record QuestionView(String id, String text, String category) { }
    public record EvidenceView(int id, String name, String description,
                               String discoveryLocation, boolean important) { }
    public record ActionRequest(String locationName, String suspectName, String questionId,
                                Integer evidenceId, List<Integer> evidenceIds) { }
    public record ActionResponse(String message, int score, boolean solved) { }
    public record ScoreView(int score, int evidenceCollected, int interrogationsCompleted) { }
    public record CaseReport(String caseId, String title, String detectiveName,
                             boolean solved, String culprit, String explanation, int score,
                             int evidenceCollected, int interrogationsCompleted,
                             List<EvidenceView> evidence) { }
}
