package com.vermeg.platform.supervision_platform.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private String username;
    private java.util.Set<String> roles;
}
