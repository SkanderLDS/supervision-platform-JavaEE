package com.vermeg.platform.supervision_platform.Service;
import com.vermeg.platform.supervision_platform.DTO.UserRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO register(UserRequestDTO dto);
    UserResponseDTO getById(Long id);
    UserResponseDTO getByUsername(String username);
    List<UserResponseDTO> getAll();
    UserResponseDTO update(Long id, UserRequestDTO dto);
    void delete(Long id);
    void assignRole(Long userId, String roleName);
}
