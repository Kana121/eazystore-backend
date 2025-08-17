package com.eazybytes.eazystore.service;

import com.eazybytes.eazystore.dto.UserDto;
import java.util.List;

public interface IUserService {
    /**
     * Retrieves all users with their details
     * @return List of UserDto containing user information
     */
    List<UserDto> getAllUsers();

    /**
     * Retrieves a user by ID
     * @param id The ID of the user to retrieve
     * @return UserDto containing the user information
     */
    UserDto getUserById(Long id);

    /**
     * Updates a user's status (enabled/disabled)
     * @param id The ID of the user to update
     * @param enabled The new status (true for enabled, false for disabled)
     * @return true if the update was successful, false otherwise
     */
    boolean updateUserStatus(Long id, boolean enabled);
}
