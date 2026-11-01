package com.enigma.model;

public class Question {
    private String id;
    private String questionText;
    private String category;
    private int points;

    public Question(String questionText, String category, int points) {
        this(category, questionText, category, points);
    }

    public Question(String id, String questionText, String category, int points) {
        this.id = id;
        this.questionText = questionText;
        this.category = category;
        this.points = points;
    }

    public String getId() { return id; }
    public String getQuestionText() { return questionText; }
    public String getCategory() { return category; }
    public int getPoints() { return points; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setCategory(String category) { this.category = category; }
    public void setPoints(int points) { this.points = points; }

    public void displayQuestion() {
        System.out.println("Question: " + questionText);
        System.out.println("Category: " + category);
        System.out.println("Points: " + points);
    }
}
