package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.NotificationDTO;
import com.amansia.amansiabackend.service.NotificationService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @GetMapping public List<NotificationDTO> list(Authentication authentication) { return service.list(authentication.getName()); }
    @GetMapping("/non-lues/count") public Map<String, Long> count(Authentication authentication) { return Map.of("count", service.count(authentication.getName())); }
    @PutMapping("/{id}/lue") public ResponseEntity<NotificationDTO> read(Authentication authentication, @PathVariable Integer id) {
        return ResponseEntity.ok(service.read(authentication.getName(), id));
    }
    @PutMapping("/tout-lire") public ResponseEntity<Void> readAll(Authentication authentication) {
        service.readAll(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
