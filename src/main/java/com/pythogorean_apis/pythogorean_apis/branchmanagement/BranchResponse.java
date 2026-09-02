package com.pythogorean_apis.pythogorean_apis.branchmanagement;

public record BranchResponse(
        Long id,
        String name) {

    public static BranchResponse from(BranchEntity branch) {
        return new BranchResponse(branch.getId(), branch.getName());
    }
}
