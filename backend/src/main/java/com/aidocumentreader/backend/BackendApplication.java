package com.aidocumentreader.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

// Import the specific enum for the DTO serialization mode
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
// --- NEW: Enforces a stable JSON structure for paginated API responses ---
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}