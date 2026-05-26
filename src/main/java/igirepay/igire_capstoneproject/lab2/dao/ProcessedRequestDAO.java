package igirepay.igire_capstoneproject.lab2.dao;

import java.sql.Timestamp;
import java.util.UUID;

public interface ProcessedRequestDAO {
    boolean existsByReferenceId(UUID referenceId);

    void create(UUID referenceId, Timestamp processedAt);
}

