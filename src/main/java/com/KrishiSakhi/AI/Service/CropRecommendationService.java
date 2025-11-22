package com.KrishiSakhi.AI.Service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
public class CropRecommendationService {

    public List<String> recommendCrops(Map<String, Object> input) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "C:\\Users\\abhis\\AppData\\Local\\Programs\\Python\\Python313\\python.exe",
                "D:\\AI\\src\\main\\resources\\ml\\predict.py"
        );

        Process process = pb.start();
        ObjectMapper mapper = new ObjectMapper();

        // send JSON to python
        try (OutputStream os = process.getOutputStream()) {
            os.write(mapper.writeValueAsBytes(input));
            os.flush();
        }

        // READ STDOUT (actual result)
        String output = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        ).lines().collect(Collectors.joining());

        // READ STDERR (warnings/errors)
        String errorOutput = new BufferedReader(
                new InputStreamReader(process.getErrorStream())
        ).lines().collect(Collectors.joining());

        // Wait for python to finish
        int exitCode = process.waitFor();

        System.out.println("PYTHON STDOUT: " + output);
        System.out.println("PYTHON STDERR: " + errorOutput);

        // ❗ Accept warnings but fail only if output is empty
        if ((output == null || output.isEmpty()) && !errorOutput.isEmpty()) {
            throw new RuntimeException("Python error: " + errorOutput);
        }

        // Return Python JSON result
        return mapper.readValue(output, new TypeReference<List<String>>() {});
    }

}
