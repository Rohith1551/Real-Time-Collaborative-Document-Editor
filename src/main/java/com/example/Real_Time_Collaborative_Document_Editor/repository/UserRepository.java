package com.example.Real_Time_Collaborative_Document_Editor.repository;

import com.example.Real_Time_Collaborative_Document_Editor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
