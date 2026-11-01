package com.enigma.api;

import com.enigma.GameController;
import com.enigma.api.ApiDtos.*;
import com.enigma.model.Evidence;
import com.enigma.model.Location;
import com.enigma.model.Question;
import com.enigma.model.Suspect;
import com.enigma.model.Victim;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** REST endpoints used by the browser. Case rules are delegated to Java game classes. */
@RestController
@RequestMapping("/api")
public class GameApiController {
    private final GameSessionService sessions;

    public GameApiController(GameSessionService sessions) { this.sessions = sessions; }

    @PostMapping("/games")
    public ResponseEntity<GameStarted> createGame(@RequestBody CreateGameRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Detective name is required.");
        }
        int age = request.age() == null ? 0 : request.age();
        if (age < 0 || age > 130) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Age must be between 0 and 130.");
        String gender = request.gender() == null || request.gender().isBlank() ? "Not specified" : request.gender().trim();
        String gameId = sessions.createGame(request.name().trim(), age, gender);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameStarted(gameId, sessions.getGame(gameId)));
    }

    @GetMapping("/games/{gameId}/case")
    public CaseFile getCase(@PathVariable String gameId) {
        GameController game = sessions.getGame(gameId);
        Victim victim = game.getVictims().get(0);
        return new CaseFile(game.getCaseId(), game.getCaseTitle(), game.getCaseDescription(),
                victim.getName(), victim.getAge(), victim.getOccupation(), victim.getLastSeenLocation());
    }

    @GetMapping("/games/{gameId}/dashboard")
    public DashboardView getDashboard(@PathVariable String gameId) {
        GameController game = sessions.getGame(gameId);
        return new DashboardView(game.getCaseId(), game.getCaseTitle(), game.getDetectiveName(),
                game.getScore(), game.getCollectedEvidence().size(), 11,
                (int) game.getLocations().stream().filter(l -> game.isLocationInvestigated(l.getName())).count(),
                game.getLocations().size(), game.getInterrogationsCompleted(), game.getObjective());
    }

    @GetMapping("/games/{gameId}/locations")
    public List<LocationView> getLocations(@PathVariable String gameId) {
        GameController game = sessions.getGame(gameId);
        return game.getLocations().stream()
                .map(location -> new LocationView(location.getName(), location.getDescription(),
                        game.isLocationInvestigated(location.getName())))
                .toList();
    }

    @GetMapping("/games/{gameId}/suspects")
    public List<SuspectView> getSuspects(@PathVariable String gameId) {
        return suspectViews(sessions.getGame(gameId));
    }

    @GetMapping("/games/{gameId}/suspects/{suspectName}")
    public SuspectView getSuspect(@PathVariable String gameId, @PathVariable String suspectName) {
        return suspectViews(sessions.getGame(gameId)).stream()
                .filter(suspect -> suspect.name().equalsIgnoreCase(suspectName)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suspect not found."));
    }

    @PostMapping("/games/{gameId}/investigations")
    public ActionResponse investigate(@PathVariable String gameId, @RequestBody ActionRequest request) {
        if (request == null || request.locationName() == null || request.locationName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location name is required.");
        }
        GameController game = sessions.getGame(gameId);
        return action(game, game.investigate(request.locationName()));
    }

    @GetMapping("/games/{gameId}/evidence")
    public List<EvidenceView> getEvidence(@PathVariable String gameId) {
        return evidenceViews(sessions.getGame(gameId));
    }

    @GetMapping("/games/{gameId}/interrogations/questions")
    public List<QuestionView> getQuestions(@PathVariable String gameId, @RequestParam String suspectName) {
        GameController game = sessions.getGame(gameId);
        List<Question> questions = game.getInterviewQuestions(suspectName);
        if (questions.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Suspect not found.");
        return questions.stream().map(q -> new QuestionView(q.getId(), q.getQuestionText(), q.getCategory())).toList();
    }

    @PostMapping("/games/{gameId}/interrogations")
    public ActionResponse askQuestion(@PathVariable String gameId, @RequestBody ActionRequest request) {
        if (request == null || request.suspectName() == null || request.suspectName().isBlank()
                || request.questionId() == null || request.questionId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suspect and question are required.");
        }
        GameController game = sessions.getGame(gameId);
        return action(game, game.interview(request.suspectName(), request.questionId()));
    }

    @PostMapping("/games/{gameId}/interrogations/evidence")
    public ActionResponse presentEvidence(@PathVariable String gameId, @RequestBody ActionRequest request) {
        if (request == null || request.suspectName() == null || request.evidenceId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suspect and evidence are required.");
        }
        GameController game = sessions.getGame(gameId);
        return action(game, game.presentEvidence(request.suspectName(), request.evidenceId()));
    }

    @PostMapping("/games/{gameId}/deductions")
    public ActionResponse submitTheory(@PathVariable String gameId, @RequestBody ActionRequest request) {
        if (request == null || request.suspectName() == null || request.suspectName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a suspect for your theory.");
        }
        GameController game = sessions.getGame(gameId);
        return action(game, game.accuse(request.suspectName(), request.evidenceIds()));
    }

    @GetMapping("/games/{gameId}/score")
    public ScoreView getScore(@PathVariable String gameId) {
        GameController game = sessions.getGame(gameId);
        return new ScoreView(game.getScore(), game.getCollectedEvidence().size(), game.getInterrogationsCompleted());
    }

    @GetMapping("/games/{gameId}/report")
    public CaseReport getReport(@PathVariable String gameId) {
        GameController game = sessions.getGame(gameId);
        return new CaseReport(game.getCaseId(), game.getCaseTitle(), game.getDetectiveName(),
                game.isSolved(), game.getCulpritName(), game.getReportSummary(), game.getScore(),
                game.getCollectedEvidence().size(), game.getInterrogationsCompleted(), evidenceViews(game));
    }

    private List<SuspectView> suspectViews(GameController game) {
        return game.getSuspects().stream().map(suspect -> {
            boolean interviewed = game.hasInterviewed(suspect.getName());
            return new SuspectView(suspect.getName(), suspect.getAge(), suspect.getGender(),
                    suspect.getRelationshipToVictim(), interviewed ? suspect.getMotive() : null,
                    interviewed ? suspect.getAlibi() : null, suspect.getSuspicionLevel(), interviewed);
        }).toList();
    }

    private GameStarted gameStarted(String gameId, GameController game) {
        Victim victim = game.getVictims().get(0);
        Integer age = game.getDetectiveAge() == 0 ? null : game.getDetectiveAge();
        return new GameStarted(gameId, new DetectiveProfile(game.getDetectiveName(), age, game.getDetectiveGender()),
                new CaseFile(game.getCaseId(), game.getCaseTitle(), game.getCaseDescription(), victim.getName(),
                        victim.getAge(), victim.getOccupation(), victim.getLastSeenLocation()),
                game.getLocationNames(), game.getSuspectNames());
    }

    private ActionResponse action(GameController game, String message) {
        return new ActionResponse(message, game.getScore(), game.isSolved());
    }

    private List<EvidenceView> evidenceViews(GameController game) {
        return game.getCollectedEvidence().stream().map(this::evidenceView).toList();
    }

    private EvidenceView evidenceView(Evidence evidence) {
        Location location = evidence.getClue() == null ? null : evidence.getClue().getLocation();
        return new EvidenceView(evidence.getId(), evidence.getName(), evidence.getDescription(),
                location == null ? "Unknown" : location.getName(), evidence.isImportant());
    }
}
