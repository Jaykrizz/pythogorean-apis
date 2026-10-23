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
        name = "enrollment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "student_id"),
                @UniqueConstraint(columnNames = "marker_id")
        })
public class Enrollment {

    public static final String DICTIONARY = "DICT_4X4_250";
    public static final int MAX_MARKER_ID = 249;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_room_id", nullable = false)
    private ClassRoom classRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;

    @Column(nullable = false)
    private int markerId;

    @Column(nullable = false)
    private String dictionary;

    protected Enrollment() {
    }

    public Enrollment(ClassRoom classRoom, AppUser student, int markerId) {
        this.classRoom = classRoom;
        this.student = student;
        this.markerId = markerId;
        this.dictionary = DICTIONARY;
    }

    public ClassRoom getClassRoom() {
        return classRoom;
    }

    public AppUser getStudent() {
        return student;
    }

    public int getMarkerId() {
        return markerId;
    }

    public String getDictionary() {
        return dictionary;
    }
}
