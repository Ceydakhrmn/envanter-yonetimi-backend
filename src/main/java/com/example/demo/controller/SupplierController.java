package com.example.demo.controller;

import com.example.demo.entity.Supplier;
import com.example.demo.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierRepository supplierRepository;

    @GetMapping
    public ResponseEntity<List<Supplier>> getAll() {
        return ResponseEntity.ok(supplierRepository.findAllOrderByName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getById(@PathVariable Long id) {
        return supplierRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping
    public ResponseEntity<Supplier> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) return ResponseEntity.badRequest().build();

        Supplier s = new Supplier();
        s.setName(name.trim());
        s.setContactName(body.get("contactName"));
        s.setEmail(body.get("email"));
        s.setPhone(body.get("phone"));
        s.setWebsite(body.get("website"));
        s.setNotes(body.get("notes"));
        return ResponseEntity.ok(supplierRepository.save(s));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return supplierRepository.findById(id).map(s -> {
            String name = body.get("name");
            if (name != null && !name.isBlank()) s.setName(name.trim());
            if (body.containsKey("contactName")) s.setContactName(body.get("contactName"));
            if (body.containsKey("email")) s.setEmail(body.get("email"));
            if (body.containsKey("phone")) s.setPhone(body.get("phone"));
            if (body.containsKey("website")) s.setWebsite(body.get("website"));
            if (body.containsKey("notes")) s.setNotes(body.get("notes"));
            return ResponseEntity.ok(supplierRepository.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!supplierRepository.existsById(id)) return ResponseEntity.notFound().build();
        supplierRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
