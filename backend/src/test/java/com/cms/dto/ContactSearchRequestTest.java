package com.cms.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContactSearchRequest.
 * Verifies pagination defaults and search/filter flag behavior.
 */
@DisplayName("ContactSearchRequest Tests")
class ContactSearchRequestTest {

    @Test
    @DisplayName("Should default pagination values and detect search criteria")
    void testDefaultsAndSearchCriteria() {
        ContactSearchRequest request = new ContactSearchRequest();

        assertEquals(0, request.getPage());
        assertEquals(20, request.getSize());
        assertEquals("createdAt", request.getSortBy());
        assertEquals("desc", request.getSortDirection());
        assertFalse(request.hasSearchCriteria());

        request.setSearchTerm("John");
        assertTrue(request.hasSearchCriteria());
    }

    @Test
    @DisplayName("Should normalize null pagination inputs")
    void testNormalization() {
        ContactSearchRequest request = new ContactSearchRequest();

        request.setPage(null);
        request.setSize(null);
        request.setSortBy(null);
        request.setSortDirection(null);

        assertEquals(0, request.getPage());
        assertEquals(20, request.getSize());
        assertEquals("createdAt", request.getSortBy());
        assertEquals("desc", request.getSortDirection());
    }
}