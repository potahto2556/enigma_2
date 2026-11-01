package com.enigma.service;

import com.enigma.model.Question;
import com.enigma.model.Suspect;

import java.util.List;

/** Supplies story-based questions and evidence reactions for suspect interviews. */
public class InterrogationService {

    public List<Question> getQuestions(Suspect suspect) {
        switch (suspect.getName()) {
            case "Noah":
                return List.of(
                        new Question("N1", "Where were you at 11:47 PM?", "Alibi", 0),
                        new Question("N2", "When did you last speak with Evan?", "Relationship", 0),
                        new Question("N3", "What do you know about Maya's old case?", "Maya's case", 0));
            case "Nora":
                return List.of(
                        new Question("R1", "Did you enter Studio 7 that night?", "Studio 7", 0),
                        new Question("R2", "Why did you hide your meeting with Evan?", "Alibi", 0),
                        new Question("R3", "What did Evan ask you about Maya?", "Maya's case", 0));
            case "Dean":
                return List.of(
                        new Question("D1", "Was the blackout an electrical failure?", "Blackout", 0),
                        new Question("D2", "Who used the security card at 11:48 PM?", "Access log", 0),
                        new Question("D3", "Why was the CCTV footage deleted?", "CCTV", 0));
            default:
                return List.of(
                        new Question("I1", "How well did you know Maya?", "Relationship", 0),
                        new Question("I2", "What did Maya tell you about that night?", "Diary", 0),
                        new Question("I3", "Why did you say you barely knew her?", "Statement", 0));
        }
    }

    public String askQuestion(Suspect suspect, String questionId, boolean hasCafeteriaFootage,
                              boolean hasFingerprint, boolean hasSecurityEvidence,
                              boolean hasDiary) {
        String name = suspect.getName();
        if ("Noah".equals(name)) {
            if ("N1".equals(questionId)) {
                return hasCafeteriaFootage
                        ? "Noah: I said I was in the cafeteria, but the footage shows I wasn't there. I lied because I had argued with Evan."
                        : "Noah: I was in the cafeteria when the power went out.";
            }
            if ("N2".equals(questionId)) return "Noah: Evan and I argued earlier. I was angry, but I did not attack him.";
            if ("N3".equals(questionId)) return "Noah: We all knew Maya. I didn't want Evan's documentary to reopen what happened five years ago.";
        }
        if ("Nora".equals(name)) {
            if ("R1".equals(questionId)) {
                return hasFingerprint
                        ? "Nora: The fingerprint is mine. I met Evan in Studio 7, but he was alive when I left."
                        : "Nora: I never went to Studio 7.";
            }
            if ("R2".equals(questionId)) return "Nora: I was afraid people would blame me. I left before Evan was attacked.";
            if ("R3".equals(questionId)) return "Nora: Evan contacted me about Maya. Five years ago, I received a warning telling me not to talk about her.";
        }
        if ("Dean".equals(name)) {
            if ("D1".equals(questionId)) return "Dean: The station lost power. It was probably an electrical problem.";
            if ("D2".equals(questionId)) {
                return hasSecurityEvidence
                        ? "Dean: I have administrative access. Someone must have taken my card."
                        : "Dean: Only authorised security staff can use the administration card.";
            }
            if ("D3".equals(questionId)) {
                return hasSecurityEvidence
                        ? "Dean: I can't explain why the footage was manually deleted."
                        : "Dean: The system went offline during the blackout.";
            }
        }
        if ("Ivy".equals(name)) {
            if ("I1".equals(questionId)) return "Ivy: Maya was my best friend. She was also my sister.";
            if ("I2".equals(questionId)) {
                return hasDiary
                        ? "Ivy: The diary is hers. She wrote that the four people with her that night shouldn't be trusted."
                        : "Ivy: I don't know what she wrote in her diary.";
            }
            if ("I3".equals(questionId)) return "Ivy: I wanted to protect Maya and didn't want the old case discussed again.";
        }
        return "The suspect has no response to that question.";
    }

    public String presentEvidence(Suspect suspect, int evidenceId) {
        String name = suspect.getName();
        if ("Dean".equals(name)) {
            if (evidenceId == 5) return "Detective: The security log shows an administration card was used at 11:48 PM.\nDean: Someone must have used my card.";
            if (evidenceId == 6) return "Detective: The CCTV was manually deleted.\nDean: I don't have an explanation for that.";
            if (evidenceId == 7) return "Detective: The torn red fabric matches your damaged uniform.\nDean: ...";
            if (evidenceId == 8) return "Detective: The old security records from Maya's case were altered.\nDean: I won't discuss an old closed case.";
            if (evidenceId == 3) return "Detective: Evan's red USB is missing.\nDean: I don't know anything about a USB.";
        }
        if ("Noah".equals(name) && evidenceId == 11) return "Detective: You are absent from the cafeteria footage.\nNoah: I wasn't there. I lied about my alibi, but I didn't attack Evan.";
        if ("Nora".equals(name) && evidenceId == 10) return "Detective: Your fingerprint was found on a coffee cup in Studio 7.\nNora: I met Evan there. He was alive when I left.";
        if ("Ivy".equals(name) && evidenceId == 9) return "Detective: Maya's diary warns against the four people with her that night.\nIvy: I was trying to keep her past private.";
        return "The evidence is added to the interview record, but it does not directly challenge this person's statement.";
    }
}
