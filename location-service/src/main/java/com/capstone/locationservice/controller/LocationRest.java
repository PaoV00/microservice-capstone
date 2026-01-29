
package com.capstone.locationservice.controller;

import com.capstone.locationservice.dto.LocationRequest;
import com.capstone.locationservice.dto.LocationResponse;
import com.capstone.locationservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/location")
public class LocationRest {

    private final LocationService service;

    @PostMapping
    public ResponseEntity<LocationResponse> create(@RequestBody LocationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<LocationResponse> getAll() {
        return service.getAll();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LocationResponse> update(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates
    ) {
        return ResponseEntity.ok(service.update(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
