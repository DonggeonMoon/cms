package com.dgmoonlabs.cms.global.security.password;

import com.dgmoonlabs.cms.global.security.RequiresReauthentication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequiresReauthentication
@RequestMapping("/api/admin/password-hash")
public class PasswordHashController {
    private final ConfigurablePasswordEncoder passwordEncoder;

    public record AlgorithmOption(PasswordHashAlgorithm value, String label, String description) {
    }

    public record PasswordHashResponse(PasswordHashAlgorithm algorithm, List<AlgorithmOption> options) {
    }

    public record PasswordHashRequest(@NotNull PasswordHashAlgorithm algorithm) {
    }

    @GetMapping
    public ResponseEntity<PasswordHashResponse> get() {
        return ResponseEntity.ok(response());
    }

    @PutMapping
    public ResponseEntity<PasswordHashResponse> change(@RequestBody @Valid final PasswordHashRequest request) {
        passwordEncoder.change(request.algorithm());
        return ResponseEntity.ok(response());
    }

    private PasswordHashResponse response() {
        return new PasswordHashResponse(passwordEncoder.current(), Arrays.stream(PasswordHashAlgorithm.values())
                .map(algorithm -> new AlgorithmOption(algorithm, algorithm.getLabel(), algorithm.getDescription()))
                .toList());
    }
}
