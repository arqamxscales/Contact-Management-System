package com.cms.service;

import com.cms.dto.ContactRequest;
import com.cms.dto.ContactResponse;
import com.cms.entity.Contact;
import com.cms.entity.User;
import com.cms.exception.ResourceNotFoundException;
import com.cms.repository.ContactRepository;
import com.cms.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ContactServiceImpl implements ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactServiceImpl(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> listContacts(String search) {
        List<Contact> contacts;
        if (StringUtils.hasText(search)) {
            contacts = contactRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(search, search);
        } else {
            contacts = contactRepository.findAll();
        }

        return contacts.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse getContact(Long id) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id " + id));
        return toResponse(contact);
    }

    @Override
    @Transactional
    public ContactResponse createContact(ContactRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + request.getUserId()));

        Contact contact = new Contact();
        contact.setUser(user);
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setAddress(request.getAddress());
        contact.setCreatedAt(LocalDateTime.now());

        Contact savedContact = contactRepository.save(contact);
        log.info("Created contact {} for user {}", savedContact.getId(), user.getId());
        return toResponse(savedContact);
    }

    private ContactResponse toResponse(Contact contact) {
        ContactResponse response = new ContactResponse();
        response.setId(contact.getId());
        response.setUserId(Objects.nonNull(contact.getUser()) ? contact.getUser().getId() : null);
        response.setFirstName(contact.getFirstName());
        response.setLastName(contact.getLastName());
        response.setTitle(contact.getTitle());
        response.setEmail(contact.getEmail());
        response.setPhone(contact.getPhone());
        response.setAddress(contact.getAddress());
        response.setCreatedAt(contact.getCreatedAt());
        return response;
    }
}