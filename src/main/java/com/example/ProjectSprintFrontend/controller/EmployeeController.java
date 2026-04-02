package com.example.ProjectSprintFrontend.controller;


import com.example.ProjectSprintFrontend.dto.Employee_DTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class EmployeeController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${backend.base-url}")
    private String BASE_API;

    // This method ensures the URL is always fresh and based on your properties
    private String getBackendUrl() {
        return BASE_API + "/employees";
    }

    @GetMapping("/")
    public String viewHomePage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer editId,
            @RequestParam(required = false) String success,
            Model model) {

        // Changed to method call
        String url = getBackendUrl() + "?projection=employeeView&page=" + page + "&size=10";

        try {
            Map officeResponse = restTemplate.getForObject(BASE_API + "/offices", Map.class);
            if (officeResponse != null && officeResponse.containsKey("_embedded")) {
                Map embedded = (Map) officeResponse.get("_embedded");
                model.addAttribute("allOffices", embedded.get("offices"));
            }

            // Changed to method call
            Map empResponse = restTemplate.getForObject(getBackendUrl() + "?size=1000", Map.class);
            if (empResponse != null && empResponse.containsKey("_embedded")) {
                Map embedded = (Map) empResponse.get("_embedded");
                model.addAttribute("allEmployees", embedded.get("employees"));
            }

            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("_embedded")) {
                Map embedded = (Map) response.get("_embedded");
                model.addAttribute("employees", embedded.get("employees"));
                model.addAttribute("pageInfo", response.get("page"));
                model.addAttribute("isSearch", false);
            }

            if (editId != null) {
                // Changed to method call
                Map employeeToEdit = restTemplate.getForObject(getBackendUrl() + "/" + editId, Map.class);
                model.addAttribute("employeeToEdit", employeeToEdit);
                model.addAttribute("editId", editId);
            }

        } catch (Exception e) {
            model.addAttribute("error", "Could not connect to backend.");
            model.addAttribute("isSearch", false);
        }
        return "employees";
    }

    @GetMapping("/search")
    public String searchEmployees(@RequestParam String type, @RequestParam String query, @RequestParam(defaultValue = "0") int page, Model model) {

        // Changed to method call
        Map empResponse = restTemplate.getForObject(getBackendUrl() + "?size=1000", Map.class);
        if (empResponse != null && empResponse.containsKey("_embedded")) {
            Map embedded = (Map) empResponse.get("_embedded");
            model.addAttribute("allEmployees", embedded.get("employees"));
        }

        Map officeResponse = restTemplate.getForObject(BASE_API + "/offices", Map.class);
        if (officeResponse != null && officeResponse.containsKey("_embedded")) {
            Map embedded = (Map) officeResponse.get("_embedded");
            model.addAttribute("allOffices", embedded.get("offices"));
        }

        // Changed to method call
        String url = getBackendUrl() + "/search/" + type + "?" + getParamName(type) + "=" + query + "&projection=employeeView&page="+ page + "&size=10";

        try {
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("_embedded")) {
                Map embedded = (Map) response.get("_embedded");
                model.addAttribute("employees", embedded.get("employees"));
                model.addAttribute("pageInfo", response.get("page"));
                model.addAttribute("isSearch", true);
                model.addAttribute("searchType", type);
                model.addAttribute("searchQuery", query);
            } else {
                model.addAttribute("employees", java.util.Collections.emptyList());
                model.addAttribute("error", "No employees found matching your search.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "No results found, check search type is proper or not");
        }
        return "employees";
    }

    @PostMapping("/addEmployee")
    public String addEmployee(@ModelAttribute Employee_DTO dto, RedirectAttributes redirectAttributes) {
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            int randomEmployeeId = new java.util.Random().nextInt(1001) + 2000;
            payload.put("employeeNumber", randomEmployeeId);
            payload.put("firstName", dto.getFirstName());
            payload.put("lastName", dto.getLastName());
            payload.put("email", dto.getEmail());
            payload.put("jobTitle", dto.getJobTitle());
            payload.put("extension", dto.getExtension());

            if (dto.getOffice() != null && !dto.getOffice().isEmpty()) {
                // FIXED: Use BASE_API instead of localhost
                payload.put("office", BASE_API + "/offices/" + dto.getOffice());
            } else {
                throw new IllegalArgumentException("Office Code is required.");
            }

            if (dto.getManager() != null && !dto.getManager().trim().isEmpty()) {
                // FIXED: Use BASE_API instead of localhost
                payload.put("manager", BASE_API + "/employees/" + dto.getManager());
            }

            // Changed to method call
            restTemplate.postForObject(getBackendUrl(), payload, String.class);
            redirectAttributes.addFlashAttribute("success", "Employee added successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred during addition.");
        }
        return "redirect:/";
    }

    @GetMapping("/customers")
    public String viewCustomers(@RequestParam("empId") Integer empId, @RequestParam(defaultValue = "0") int page, Model model) {

        // FIXED: Use BASE_API instead of localhost
        String url = BASE_API + "/customer/search/by-employee?employeeNumber=" + empId
                + "&projection=CustomerListProjection&page=" + page + "&size=10";

        try {
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("_embedded")) {
                Map embedded = (Map) response.get("_embedded");
                model.addAttribute("customers", embedded.get("customers"));
                model.addAttribute("pageInfo", response.get("page"));
            }
            model.addAttribute("empId", empId);
        } catch (Exception e) {
            model.addAttribute("error", "Could not fetch customers.");
        }
        return "employee-customers";
    }

    @PutMapping("/updateJobTitle")
    public String updateJobTitle(@RequestParam("id") Integer id, @RequestParam("jobTitle") String jobTitle, RedirectAttributes ra) {
        // Changed to method call
        String url = getBackendUrl() + "/" + id;
        try {
            Map<String, Object> existingEmployee = restTemplate.getForObject(url, Map.class);
            if (existingEmployee != null) {
                existingEmployee.put("jobTitle", jobTitle);
                restTemplate.put(url, existingEmployee);
                ra.addFlashAttribute("success", "Job Title updated!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Update failed.");
        }
        return "redirect:/";
    }

    // 5. UPDATE OFFICE (AJAX/JS)
    @PutMapping("/updateOffice")
    @ResponseBody
    public ResponseEntity<?> updateOffice(@RequestBody Map<String, Object> payload) {
        try {
            Integer id = Integer.parseInt(payload.get("id").toString());
            String officeId = payload.get("officeId").toString();

            String url = getBackendUrl() + "/" + id + "/office";
            String fullUri = BASE_API + "/offices/" + officeId;

            sendUriRequest(url, fullUri);
            return ResponseEntity.ok().body(Map.of("message", "Office linked!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Office update failed.");
        }
    }

    @PutMapping("/updateManager")
    @ResponseBody
    public ResponseEntity<?> updateManager(@RequestBody Map<String, Object> payload) {
        try {
            Integer id = Integer.parseInt(payload.get("id").toString());
            String managerId = payload.get("managerId").toString();

            // Changed to method call + updated hardcoded URI
            String url = getBackendUrl() + "/" + id + "/manager";
            String fullUri = BASE_API + "/employees/" + managerId;

            sendUriRequest(url, fullUri);
            return ResponseEntity.ok().body(Map.of("message", "Manager updated"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // Helper for search mapping
    private String getParamName(String type) {
        return switch (type) {
            case "byName" -> "name";
            case "byOfficeCode" -> "officeCode";
            case "byReportingManagerName" -> "managerName";
            case "byJobTitle" -> "jobTitle";
            default -> "name";
        };
    }

    private void sendUriRequest(String url, String fullUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "uri-list"));
        HttpEntity<String> entity = new HttpEntity<>(fullUri, headers);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }
}
