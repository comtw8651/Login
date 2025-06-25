package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.WatchProgress;

public interface WatchProgressRepository extends JpaRepository<WatchProgress, Long> {
} 