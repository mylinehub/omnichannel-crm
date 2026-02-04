package com.mylinehub.crm.rag.repository.impl;

import com.mylinehub.crm.rag.dto.ResultDTO;
import com.mylinehub.crm.rag.repository.EmbeddingRepositoryCustom;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class EmbeddingRepositoryCustomImpl implements EmbeddingRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    public EmbeddingRepositoryCustomImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ResultDTO> findNearestByVector(String organization, float[] vector, int limit) {
        int finalLimit = Math.max(limit, 1);
        String literal = "'[" + toCommaSeparated(vector) + "]'::vector";
        double distanceThreshold = 6.0;

        String sql =
                "WITH computed AS ( " +
                        "SELECT " +
                        " e.id AS embeddingId, " +
                        " e.organization AS organization, " +
                        " dc.id AS documentChunkId, " +
                        " dc.text AS chunkText, " +
                        " d.id AS documentId, " +
                        " d.original_filename AS originalFilename, " +
                        " d.file_hash AS fileHash, " +
                        " e.vector <-> " + literal + " AS distance " +
                        "FROM embedding e " +
                        "JOIN document_chunk dc ON e.document_chunk_id = dc.id " +
                        "JOIN document d ON dc.document_id = d.id " +
                        "WHERE e.organization = ? " +
                        ") " +
                        "SELECT *, RANK() OVER (ORDER BY distance ASC) AS rank " +
                        "FROM computed " +
                        "WHERE distance <= ? " +
                        "ORDER BY distance ASC " +
                        "LIMIT ?";


        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(ResultDTO.class),
                organization,
                distanceThreshold,
                finalLimit
        );
    }

    private String toCommaSeparated(float[] v) {
        return IntStream.range(0, v.length)
                .mapToObj(i -> Float.toString(v[i]))
                .collect(Collectors.joining(","));
    }
}
