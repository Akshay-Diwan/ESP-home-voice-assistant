// package com.akshay.assistant.tools.calendar.entity;

// import java.time.LocalDate;
// import java.util.UUID;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;

// @Entity
// @Table(name = "events")
// public class EventEntity{
//     @Id
//     @Column(name = "event_name", nullable=false, updatable=false)
//     @GeneratedValue(strategy = GenerationType.UUID)
//     private UUID event_id;

//     @Column(name = "event_name", nullable=false)
//     private String event_name;

//     @Column(name = "start_time", nullable=false)
//     private LocalDate start_time;

//     @Column(name = "end_time", nullable=false)
//     private LocalDate end_time;
    
//     private String description;

//     public EventEntity() {}

// }