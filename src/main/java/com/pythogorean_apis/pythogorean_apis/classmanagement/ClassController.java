package com.pythogorean_apis.pythogorean_apis.classmanagement;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@SecurityRequirement(name = "bearerAuth")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @PostMapping
    public ResponseEntity<CreateClassResponse> createClass(@RequestBody CreateClassRequest request) {

        CreateClassResponse body = CreateClassResponse.from(classService.createClass(request.getName()));

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public List<CreateClassResponse> getMyClasses() {
        return classService.getMyClasses().stream()
                .map(CreateClassResponse::from)
                .toList();
    }

    @GetMapping("/{classId}")
    public CreateClassResponse getClass(@PathVariable Long classId) {
        return CreateClassResponse.from(classService.getOwnedClass(classId));
    }
}
