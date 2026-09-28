package com.musomi.desktop.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User data transfer object representing authenticated or listed user details.
 * Matches user object schema defined in API_CONTRACT.md.
 *
 * @param id         unique user identifier
 * @param username   login username
 * @param fullName   display full name of the user
 * @param email      user email address
 * @param phone      user telephone number
 * @param role       user role (e.g., "ADMIN", "TEACHER", "STUDENT")
 * @param schoolId   affiliated school identifier
 * @param schoolName affiliated school display name
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserResponse(
    @JsonProperty("id")
    Long id,

    @JsonProperty("username")
    String username,

    @JsonProperty("fullName")
    String fullName,

    @JsonProperty("email")
    String email,

    @JsonProperty("phone")
    String phone,

    @JsonProperty("role")
    String role,

    @JsonProperty("schoolId")
    Long schoolId,

    @JsonProperty("schoolName")
    String schoolName
) {

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }
}
