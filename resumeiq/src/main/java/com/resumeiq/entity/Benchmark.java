package com.resumeiq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "benchmarks")
@Getter
@Setter
@NoArgsConstructor
public class Benchmark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "industry", nullable = false)
    private String industry;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "avg_score", nullable = false)
    private Double avgScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "percentile_data", nullable = false, columnDefinition = "jsonb")
    private String percentileData;
}