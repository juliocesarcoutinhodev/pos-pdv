package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.interfaces.dto.UserSessionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserSessionResponse> me() {
        var auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var response = new UserSessionResponse(auth.getId(), auth.getEmail(), auth.getName(), auth.getRoles());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Admin area");
    }
}
