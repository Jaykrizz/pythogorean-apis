package com.pythogorean_apis.pythogorean_apis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pythogorean_apis.pythogorean_apis.domain.AppUser;
import com.pythogorean_apis.pythogorean_apis.domain.Role;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<AppUser> findByBranch_IdAndRoleOrderByNameAsc(Long branchId, Role role);
}
