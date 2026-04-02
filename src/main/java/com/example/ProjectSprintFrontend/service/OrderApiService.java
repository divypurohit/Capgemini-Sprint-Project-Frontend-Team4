package com.example.ProjectSprintFrontend.service;

import com.example.ProjectSprintFrontend.dto.OrderDetailDto;
import com.example.ProjectSprintFrontend.dto.OrderDetailsResponse;
import com.example.ProjectSprintFrontend.dto.OrderDto;
import com.example.ProjectSprintFrontend.dto.OrdersPageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderApiService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public OrderApiService(RestTemplate restTemplate,
                           @Value("${backend.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    // ── Orders list ─────────────────────────────────────────────
    public OrdersPageResponse getOrders(int page, int size, String sort, String dir, String status) {

        UriComponentsBuilder uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/orders")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort + "," + dir);

        if (status != null && !status.isBlank()) {
            uri = UriComponentsBuilder
                    .fromUriString(baseUrl + "/orders/search/byStatus")
                    .queryParam("status", status)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .queryParam("sort", sort + "," + dir);
        }

        try {
            OrdersPageResponse response =
                    restTemplate.getForObject(uri.toUriString(), OrdersPageResponse.class);

            return response != null ? response : new OrdersPageResponse();

        } catch (Exception e) {
            return new OrdersPageResponse();
        }
    }

    // ── Search by customer ──────────────────────────────────────
    public OrdersPageResponse searchByCustomer(int customerNumber,
                                               int page, int size,
                                               String sort, String dir) {

        UriComponentsBuilder uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/orders/search/byCustomer")
                .queryParam("customerNumber", customerNumber)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort + "," + dir);

        try {
            OrdersPageResponse resp =
                    restTemplate.getForObject(uri.toUriString(), OrdersPageResponse.class);

            return resp != null ? resp : new OrdersPageResponse();

        } catch (Exception e) {
            return new OrdersPageResponse();
        }
    }

    // ── Get single order ────────────────────────────────────────
    public OrderDto getOrder(Integer id) {
        try {
            return restTemplate.getForObject(
                    baseUrl + "/orders/" + id,
                    OrderDto.class
            );
        } catch (Exception e) {
            return null;
        }
    }

    // ── Order Details ───────────────────────────────────────────
    public List<OrderDetailDto> getOrderDetails(Integer orderNumber) {

        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/orderdetails/search/findByOrder_OrderNumber")
                .queryParam("orderNumber", orderNumber)
                .queryParam("projection", "orderDetailView")
                .toUriString();

        try {
            OrderDetailsResponse resp =
                    restTemplate.getForObject(url, OrderDetailsResponse.class);

            return resp != null
                    ? resp.getOrderDetails()
                    : Collections.emptyList();

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // ── Create Order ────────────────────────────────────────────
    public OrderDto createOrder(Integer orderNumber,
                                Integer customerNumber,
                                String orderDate,
                                String requiredDate,
                                String shippedDate,
                                String status,
                                String comments) {

        Map<String, Object> body = new HashMap<>();
        body.put("orderNumber", orderNumber);
        body.put("orderDate", orderDate);
        body.put("requiredDate", requiredDate);
        body.put("status", status);

        if (shippedDate != null && !shippedDate.isBlank())
            body.put("shippedDate", shippedDate);

        if (comments != null && !comments.isBlank())
            body.put("comments", comments);

        body.put("customer", "/customers/" + customerNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(
                    baseUrl + "/orders",
                    entity,
                    OrderDto.class
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create order: " + e.getMessage(), e
            );
        }
    }

    // ── Update Order ────────────────────────────────────────────
    public OrderDto updateOrder(Integer orderNumber,
                                String orderDate,
                                String requiredDate,
                                String shippedDate,
                                String status,
                                String comments) {

        Map<String, Object> body = new HashMap<>();

        if (orderDate != null && !orderDate.isBlank())
            body.put("orderDate", orderDate);

        if (requiredDate != null && !requiredDate.isBlank())
            body.put("requiredDate", requiredDate);

        if (status != null && !status.isBlank())
            body.put("status", status);

        body.put("shippedDate",
                (shippedDate != null && !shippedDate.isBlank())
                        ? shippedDate : null);

        body.put("comments",
                (comments != null && !comments.isBlank())
                        ? comments : null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/orders/" + orderNumber,
                    HttpMethod.PATCH,
                    entity,
                    OrderDto.class
            ).getBody();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to update order: " + e.getMessage(), e
            );
        }
    }

    // ── Count by Status ─────────────────────────────────────────
    public long countByStatus(String status) {

        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/orders/search/byStatus")
                .queryParam("status", status)
                .queryParam("size", 1)
                .toUriString();

        try {
            OrdersPageResponse resp =
                    restTemplate.getForObject(url, OrdersPageResponse.class);

            if (resp != null && resp.getPage() != null)
                return resp.getPage().getTotalElements();

        } catch (Exception ignored) {}

        return 0;
    }

    // ── Count All ───────────────────────────────────────────────
    public long countAll() {

        try {
            OrdersPageResponse resp =
                    restTemplate.getForObject(
                            baseUrl + "/orders?size=1",
                            OrdersPageResponse.class
                    );

            if (resp != null && resp.getPage() != null)
                return resp.getPage().getTotalElements();

        } catch (Exception ignored) {}

        return 0;
    }
}
