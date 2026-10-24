package com.pythogorean_apis.pythogorean_apis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false, length = 500)
    private String prompt;

    @Column(nullable = false, length = 200)
    private String optionA;

    @Column(nullable = false, length = 200)
    private String optionB;

    @Column(nullable = false, length = 200)
    private String optionC;

    @Column(nullable = false, length = 200)
    private String optionD;

    @Column(nullable = false, length = 1)
    private String correctLetter;

    @Column(nullable = false)
    private int marks;

    @Column(nullable = false)
    private int sortOrder;

    protected Question() {
    }

    public Question(
            Topic topic,
            String prompt,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctLetter,
            int marks,
            int sortOrder) {
        this.topic = topic;
        this.prompt = prompt;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctLetter = correctLetter;
        this.marks = marks;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public String getCorrectLetter() {
        return correctLetter;
    }

    public int getMarks() {
        return marks;
    }
}
