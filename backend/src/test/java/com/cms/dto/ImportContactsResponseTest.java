package com.cms.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ImportContactsResponseTest {

    @Test
    void gettersAndSettersWork() {
        ImportContactsResponse resp = new ImportContactsResponse();
        resp.setTotalProcessed(5);
        resp.setSuccessCount(4);
        resp.setFailureCount(1);
        resp.setErrors(List.of("Row 3: firstName is required"));
        resp.setMessage("Import completed: 4 succeeded, 1 failed out of 5 total rows");

        assertEquals(5, resp.getTotalProcessed());
        assertEquals(4, resp.getSuccessCount());
        assertEquals(1, resp.getFailureCount());
        assertNotNull(resp.getErrors());
        assertTrue(resp.getErrors().get(0).contains("firstName"));
        assertTrue(resp.getMessage().contains("4 succeeded"));
    }
}
