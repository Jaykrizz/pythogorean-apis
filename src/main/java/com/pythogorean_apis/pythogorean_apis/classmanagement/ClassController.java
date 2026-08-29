package com.pythogorean_apis.pythogorean_apis.classmanagement;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @PostMapping
    public CreateClassResponse createClass(
            @RequestBody CreateClassRequest request) {

        ClassEntity saved = classService.createClass(request.getName());
        return new CreateClassResponse(saved.getId().intValue(), saved.getName());
    }
}