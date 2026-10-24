package com.pythogorean_apis.pythogorean_apis.domain;

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
        name = "class_subject",
        uniqueConstraints = @UniqueConstraint(columnNames = {"class_room_id", "subject_id"}))
public class ClassSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_room_id", nullable = false)
    private ClassRoom classRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    protected ClassSubject() {
    }

    public ClassSubject(ClassRoom classRoom, Subject subject) {
        this.classRoom = classRoom;
        this.subject = subject;
    }

    public Subject getSubject() {
        return subject;
    }
}
