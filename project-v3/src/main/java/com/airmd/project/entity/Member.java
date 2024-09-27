package com.airmd.project.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Member{
    @Id
    @Column(name = "member_id", columnDefinition = "BINARY(16)")
    @GeneratedValue
    private UUID id;

    @Column(length = 100, unique = true, nullable = false)
    private String email;
    private String password;



    @Enumerated(EnumType.STRING)
    private Role role;


}