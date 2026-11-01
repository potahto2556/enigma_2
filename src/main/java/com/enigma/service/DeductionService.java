package com.enigma.service;

import com.enigma.model.Suspect;

import java.util.Set;

public class DeductionService {
    private static final Set<Integer> REQUIRED_EVIDENCE = Set.of(3, 4, 5, 6, 7, 8);

    public Suspect findMostSuspicious(java.util.List<Suspect> suspects) {
        if (suspects == null || suspects.isEmpty()) return null;
        Suspect mostSuspicious = suspects.get(0);
        for (Suspect suspect : suspects) {
            if (suspect.getSuspicionLevel() > mostSuspicious.getSuspicionLevel()) mostSuspicious = suspect;
        }
        return mostSuspicious;
    }

    public boolean checkCulprit(Suspect suspect) {
        return suspect != null && suspect.isGuilty();
    }

    public boolean hasRequiredEvidence(Set<Integer> evidenceIds) {
        return evidenceIds != null && evidenceIds.containsAll(REQUIRED_EVIDENCE);
    }

    public boolean validateTheory(Suspect suspect, Set<Integer> evidenceIds) {
        return checkCulprit(suspect) && hasRequiredEvidence(evidenceIds);
    }

    public String getDeductionMessage(Suspect suspect) {
        if (suspect == null) return "No suspect selected.";
        if (suspect.getSuspicionLevel() >= 80) return suspect.getName() + " is highly suspicious. Investigate further!";
        if (suspect.getSuspicionLevel() >= 50) return suspect.getName() + " is moderately suspicious.";
        return suspect.getName() + " currently has a low suspicion level.";
    }
}
