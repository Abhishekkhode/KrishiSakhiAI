package com.KrishiSakhi.AI.Controller;

import com.KrishiSakhi.AI.Service.CropRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    @Autowired
    private CropRecommendationService service;


    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(@RequestBody Map<String, Object> input) {
        try {
            List<String> recs = service.recommendCrops(input);
            return ResponseEntity.ok(Map.of("recommendations", recs));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


}
