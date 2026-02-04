package com.mylinehub.crm.security.jwt;

import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.repository.EmployeeRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

public class JwtVerify {

    public Employee verifyTokenOrThrowError(String token, SecretKey secretKey, EmployeeRepository employeeRepository) {

        final String USER_NOT_FOUND_MSG = "user with email %s not found";

        // -----------------------------
        // Parse JWT
        // -----------------------------
        Jws<Claims> claimsJws =
                Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

        Claims body = claimsJws.getBody();
        String username = body.getSubject(); // email

        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, "null"));
        }

        // normalize email key
        String emailKey = username.trim().toLowerCase();

        // -----------------------------
        // 1) TRY MEMORY (email -> ext -> employee)
        // -----------------------------
        Employee memEmployee = findEmployeeInMemoryByEmail(emailKey);
        if (memEmployee != null) {
            return memEmployee;
        }

        // -----------------------------
        // 2) FALLBACK TO DB
        //    (and ALSO populate memory maps so next time it's fast)
        // -----------------------------
        Employee dbEmployee = employeeRepository.findByEmail(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format(USER_NOT_FOUND_MSG, username)));

        // Populate memory if possible
        try {
            String ext = (dbEmployee.getExtension() != null) ? dbEmployee.getExtension().trim() : null;

            if (ext != null && !ext.isEmpty()) {

                // Update extension -> dto
                EmployeeDataAndStateDTO dto = new EmployeeDataAndStateDTO();
                dto.setEmployee(dbEmployee);
                // do NOT touch states here; RefreshBackEndConnectionRunnable will keep them correct
                EmployeeDataAndState.workOnAllEmployeeDataAndState(ext, dto, "update");

                // Update email -> extension
                if (dbEmployee.getEmail() != null && !dbEmployee.getEmail().trim().isEmpty()) {
                    EmployeeDataAndState.workOnAllEmployeeEmailAndExtension(
                            dbEmployee.getEmail().trim().toLowerCase(),
                            ext,
                            "update"
                    );
                }

                // Update phone -> extension (optional but helpful)
                if (dbEmployee.getPhonenumber() != null && !dbEmployee.getPhonenumber().trim().isEmpty()) {
                    EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(
                            dbEmployee.getPhonenumber().trim(),
                            ext,
                            "update"
                    );
                }
            }
        } catch (Exception ignore) {
            // do nothing: fallback must never fail auth
        }

        return dbEmployee;
    }

    /**
     * Memory lookup:
     * - email -> extension (O(1))
     * - extension -> EmployeeDataAndStateDTO -> Employee
     */
    private Employee findEmployeeInMemoryByEmail(String emailKeyLower) {
        if (emailKeyLower == null || emailKeyLower.trim().isEmpty()) return null;

        String emailKey = emailKeyLower.trim().toLowerCase();

        // 1) email -> extension
        Map<String, String> emailToExt =
                EmployeeDataAndState.workOnAllEmployeeEmailAndExtension(emailKey, null, "get-one");

        if (emailToExt == null || emailToExt.isEmpty()) return null;

        String extension = emailToExt.get(emailKey);
        if (extension == null || extension.trim().isEmpty()) return null;

        String ext = extension.trim();

        // 2) extension -> employee dto
        Map<String, EmployeeDataAndStateDTO> extToDto =
                EmployeeDataAndState.workOnAllEmployeeDataAndState(ext, null, "get-one");

        if (extToDto == null || extToDto.isEmpty()) return null;

        EmployeeDataAndStateDTO dto = extToDto.get(ext);
        if (dto == null) return null;

        Employee e = dto.getEmployee();
        if (e == null) return null;

        // final safety: ensure the email matches (prevents wrong mapping if memory corrupted)
        String eEmail = e.getEmail();
        if (eEmail != null && eEmail.trim().toLowerCase().equals(emailKey)) {
            return e;
        }

        return null;
    }
}
