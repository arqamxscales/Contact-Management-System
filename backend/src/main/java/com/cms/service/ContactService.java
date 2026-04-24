package com.cms.service;

import com.cms.dto.ContactRequest;
import com.cms.dto.ContactResponse;
import java.util.List;

public interface ContactService {

    List<ContactResponse> listContacts(String search);

    ContactResponse getContact(Long id);

    ContactResponse createContact(ContactRequest request);
}