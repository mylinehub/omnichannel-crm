package com.mylinehub.crm.rag.repository;


import java.util.List;

import com.mylinehub.crm.rag.dto.ResultDTO;

public interface EmbeddingRepositoryCustom {
	List<ResultDTO> findNearestByVector(String organization, float[] vector, int limit);
}
