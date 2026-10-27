package com.campusbrain.dto;

import java.time.LocalDateTime;

public class RealtimeEvent {
    private String eventType;
    private Long materialId;
    private String category;
    private String timestamp;
    private String message;
    private Object payload;

    public RealtimeEvent() {
        this.timestamp = LocalDateTime.now().toString();
    }

    public RealtimeEvent(String eventType, Long materialId, String category, String message, Object payload) {
        this.eventType = eventType;
        this.materialId = materialId;
        this.category = category;
        this.message = message;
        this.payload = payload;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
