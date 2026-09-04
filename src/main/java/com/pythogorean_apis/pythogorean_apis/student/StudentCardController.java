package com.pythogorean_apis.pythogorean_apis.student;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students/{studentId}/card")
@SecurityRequirement(name = "bearerAuth")
public class StudentCardController {

    private final StudentService studentService;

    public StudentCardController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getCard(@PathVariable Long studentId) {

        ArucoCardEntity card = studentService.getCard(studentId);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("pythogorean-card-" + card.getMarkerId() + ".png")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header("Content-Disposition", disposition.toString())
                .body(card.getImage());
    }

    @PostMapping
    public StudentResponse regenerateCard(@PathVariable Long studentId) {
        return studentService.regenerateCard(studentId);
    }
}
