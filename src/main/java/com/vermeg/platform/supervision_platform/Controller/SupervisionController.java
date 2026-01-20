package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.Service.SupervisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supervision")
public class SupervisionController {

    private final SupervisionService supervisionService;

    public SupervisionController(SupervisionService supervisionService) {
        this.supervisionService = supervisionService;
    }

    @PostMapping("/servers/{id}")
    public ResponseEntity<String> supervise(@PathVariable Long id) {
        supervisionService.superviseServer(id);
        return ResponseEntity.ok("Supervision executed for server " + id);
    }
}
