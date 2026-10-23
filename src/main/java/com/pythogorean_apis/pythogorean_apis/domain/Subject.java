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
        uniqueConstraints = @UniqueConstraint(columnNames = {"class_room_id", "name"}))
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_room_id", nullable = false)
    private ClassRoom classRoom;

    @Column(nullable = false, length = 80)
    private String name;

    protected Subject() {
    }

    public Subject(ClassRoom classRoom, String name) {
        this.classRoom = classRoom;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public ClassRoom getClassRoom() {
        return classRoom;
    }

    public String getName() {
        return name;
    }
}
