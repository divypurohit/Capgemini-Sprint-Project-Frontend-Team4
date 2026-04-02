package com.example.ProjectSprintFrontend.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Maps the HAL response for GET /orderdetails?...&projection=orderDetailView:
 * {
 *   "_embedded": { "orderdetails": [...] },
 *   "_page": { ... }
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetailsResponse {

    @JsonProperty("_embedded")
    private Map<String, List<OrderDetailDto>> embedded;

    public List<OrderDetailDto> getOrderDetails() {
        if (embedded == null) return Collections.emptyList();
        List<OrderDetailDto> result = embedded.get("orderDetailses");
        if (result == null) result = embedded.get("orderdetails");
        if (result == null) result = embedded.get("orderDetails");
        return result != null ? result : Collections.emptyList();
    }
    

    public void setEmbedded(Map<String, List<OrderDetailDto>> v) { this.embedded = v; }
}

