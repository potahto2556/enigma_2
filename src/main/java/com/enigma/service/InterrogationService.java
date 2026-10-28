package com.enigma.service;

import com.enigma.model.Suspect;
import java.util.Scanner;

/** Runs a short, choice-based conversation with a suspect. */
public class InterrogationService {

    public void interrogate(Suspect suspect, Scanner scanner, boolean hasSecurityEvidence) {
        System.out.println("\n===== INTERROGATION: " + suspect.getName().toUpperCase() + " =====");
        System.out.println("Detective: " + suspect.getName() + ", where were you during the blackout?");

        if (suspect.getName().equals("Noah")) {
            System.out.println("Noah: I was in the cafeteria. Evan and I argued earlier, but I did not hurt him.");
            System.out.println("Detective: Why hide that you argued with Evan?");
            System.out.println("Noah: I knew he was investigating what happened to Maya. I did not want to be dragged into it.");
        } else if (suspect.getName().equals("Nora")) {
            System.out.println("Nora: I met Evan in Studio 7, but he was alive when I left.");
            System.out.println("Detective: Why did you first say you were in your dressing room?");
            System.out.println("Nora: I was afraid people would blame me. I went there to talk about Maya.");
        } else if (suspect.getName().equals("Ivy")) {
            System.out.println("Ivy: Maya was my sister. I said I barely knew her because I wanted to protect her memory.");
            System.out.println("Detective: Do you know why Evan reopened her case?");
            System.out.println("Ivy: He found an old security record. He thought someone changed it.");
        } else {
            System.out.println("Dean: The blackout was an electrical failure. I was checking the security system.");
            if (hasSecurityEvidence) {
                System.out.println("Detective: A security card was used at 11:48, and the blackout footage was manually deleted.");
                System.out.println("Dean: Someone must have used my card.");
                suspect.setSuspicionLevel(Math.min(100, suspect.getSuspicionLevel() + 15));
            } else {
                System.out.println("Detective: Who can access the security system?");
                System.out.println("Dean: I can, along with a few senior security staff.");
            }
        }
        System.out.println("\nSuspicion level: " + suspect.getSuspicionLevel() + "/100");
    }
}
