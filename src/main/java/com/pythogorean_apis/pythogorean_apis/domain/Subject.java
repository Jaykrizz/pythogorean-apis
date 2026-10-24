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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "subject",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "name"}))
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(nullable = false, length = 80)
    private String name;

    protected Subject() {
    }

    public Subject(Branch branch, String name) {
        this.branch = branch;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Branch getBranch() {
        return branch;
    }

    public String getName() {
        return name;
    }
}
