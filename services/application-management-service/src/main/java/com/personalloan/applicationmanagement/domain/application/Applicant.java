package com.personalloan.applicationmanagement.domain.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Applicant {

    private final UUID applicantId;
    private final UUID applicationId;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final Citizenship citizenship;
    private final String ssnToken;
    private final String email;
    private final String phone;
    private final String street;
    private final String city;
    private final String state;
    private final String zip;
    private final String employerName;
    private final EmploymentStatus employmentStatus;
    private final BigDecimal annualIncome;
    private final LocalDateTime createdTimestamp;

    private Applicant(UUID applicantId, UUID applicationId, String firstName, String lastName,
                       LocalDate dateOfBirth, Citizenship citizenship, String ssnToken,
                       String email, String phone, String street, String city, String state, String zip,
                       String employerName, EmploymentStatus employmentStatus, BigDecimal annualIncome,
                       LocalDateTime createdTimestamp) {
        this.applicantId = applicantId;
        this.applicationId = applicationId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.citizenship = citizenship;
        this.ssnToken = ssnToken;
        this.email = email;
        this.phone = phone;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.employerName = employerName;
        this.employmentStatus = employmentStatus;
        this.annualIncome = annualIncome;
        this.createdTimestamp = createdTimestamp;
    }

    public static Applicant create(UUID applicationId, String firstName, String lastName,
                                    LocalDate dateOfBirth, Citizenship citizenship, String ssnToken,
                                    String email, String phone, String street, String city, String state, String zip,
                                    String employerName, EmploymentStatus employmentStatus, BigDecimal annualIncome) {
        return new Applicant(UUID.randomUUID(), applicationId, firstName, lastName, dateOfBirth,
                citizenship, ssnToken, email, phone, street, city, state, zip,
                employerName, employmentStatus, annualIncome, LocalDateTime.now());
    }

    public static Applicant reconstitute(UUID applicantId, UUID applicationId, String firstName, String lastName,
                                          LocalDate dateOfBirth, Citizenship citizenship, String ssnToken,
                                          String email, String phone, String street, String city, String state, String zip,
                                          String employerName, EmploymentStatus employmentStatus, BigDecimal annualIncome,
                                          LocalDateTime createdTimestamp) {
        return new Applicant(applicantId, applicationId, firstName, lastName, dateOfBirth,
                citizenship, ssnToken, email, phone, street, city, state, zip,
                employerName, employmentStatus, annualIncome, createdTimestamp);
    }

    public UUID getApplicantId() { return applicantId; }
    public UUID getApplicationId() { return applicationId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public Citizenship getCitizenship() { return citizenship; }
    public String getSsnToken() { return ssnToken; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZip() { return zip; }
    public String getEmployerName() { return employerName; }
    public EmploymentStatus getEmploymentStatus() { return employmentStatus; }
    public BigDecimal getAnnualIncome() { return annualIncome; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
}
