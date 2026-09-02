package com.pythogorean_apis.pythogorean_apis.branchmanagement;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchRepository branchRepository;

    public BranchController(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @GetMapping
    public List<BranchResponse> getBranches() {
        return branchRepository.findAll().stream()
                .map(BranchResponse::from)
                .toList();
    }
}
