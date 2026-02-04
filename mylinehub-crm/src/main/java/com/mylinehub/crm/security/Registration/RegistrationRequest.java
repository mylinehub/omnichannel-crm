package com.mylinehub.crm.security.Registration;

import com.mylinehub.crm.entity.Departments;
import lombok.*;

import java.util.Date;
import java.util.TimeZone;

/**
 * the class contains the fields that are required in the registration form
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class RegistrationRequest {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;
    private final String pesel;
    private final String sex;
    private final Date birthdate;
    private final Double salary;
    private final Departments department;
    private final String phoneContext;
    private final String organization;
    private final String extension;
    private final String extensionpassword;
    private final String domain;
    private final TimeZone timezone;
    private final String phonenumber;
    private final String transfer_phone_1;
    private final String transfer_phone_2;
}
