package com.urlshortener.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.urlshortener.entity.ClickEvent;

public interface ClickEventRepository extends JpaRepository<ClickEvent, UUID> {
}