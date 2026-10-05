package com.amansia.amansiabackend.dto;

import com.amansia.amansiabackend.entity.TypeNotification;
import java.time.LocalDateTime;

public record NotificationDTO(Integer id, TypeNotification type, String titre, String message,
                              String lien, Boolean estLue, LocalDateTime dateCreation) {}
