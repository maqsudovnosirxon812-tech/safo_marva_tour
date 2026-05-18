package com.safomarva.tour.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
public class LeadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String phone;

    // Mapping 'package' DB column which is a reserved keyword in Java
    @Column(name = "package", nullable = false, length = 100)
    private String packageSelected;

    @Column(length = 100)
    private String room = "Kiritilmagan";

    @Column(length = 50)
    private String source = "web-site";

    @Column(length = 50)
    private String operator = "Erkak";

    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "Naqd pul";

    @Column(name = "persons", nullable = false)
    private Integer persons = 1;

    @Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // Constructors
    public LeadEntity() {
    }

    public LeadEntity(String name, String phone, String packageSelected, String room, String source) {
        this.name = name;
        this.phone = phone;
        this.packageSelected = packageSelected;
        this.room = room != null ? room : "Kiritilmagan";
        this.source = source != null ? source : "web-site";
        this.persons = 1;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPackageSelected() {
        return packageSelected;
    }

    public void setPackageSelected(String packageSelected) {
        this.packageSelected = packageSelected;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator != null ? operator : "Erkak";
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Naqd pul";
    }

    public Integer getPersons() {
        return persons;
    }

    public void setPersons(Integer persons) {
        this.persons = persons != null ? persons : 1;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
