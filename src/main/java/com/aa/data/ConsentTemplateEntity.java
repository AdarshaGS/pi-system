package com.aa.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consent_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "entity_type", nullable = false)
    private int entityType;
}
