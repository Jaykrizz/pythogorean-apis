package com.pythogorean_apis.pythogorean_apis.student;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "aruco_cards")
public class ArucoCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private StudentEntity student;

    @Column(name = "marker_id", nullable = false)
    private Integer markerId;

    @Column(name = "aruco_dictionary", nullable = false, length = 32)
    private String arucoDictionary;

    @Column(name = "image", nullable = false)
    private byte[] image;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.generatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public StudentEntity getStudent() {
        return student;
    }

    public void setStudent(StudentEntity student) {
        this.student = student;
    }

    public Integer getMarkerId() {
        return markerId;
    }

    public void setMarkerId(Integer markerId) {
        this.markerId = markerId;
    }

    public String getArucoDictionary() {
        return arucoDictionary;
    }

    public void setArucoDictionary(String arucoDictionary) {
        this.arucoDictionary = arucoDictionary;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}
