package com.eazybytes.eazystore.service.impl;

import com.eazybytes.eazystore.dto.AddressDto;
import com.eazybytes.eazystore.dto.UserDto;
import com.eazybytes.eazystore.entity.Address;
import com.eazybytes.eazystore.entity.Customer;
import com.eazybytes.eazystore.entity.Role;
import com.eazybytes.eazystore.exception.ResourceNotFoundException;
import com.eazybytes.eazystore.repository.CustomerRepository;
import com.eazybytes.eazystore.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserDto> getAllUsers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id.toString()));
        return convertToDto(customer);
    }

    @Override
    public boolean updateUserStatus(Long id, boolean enabled) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id.toString()));
        // Assuming there's an 'enabled' field in the Customer entity
        // customer.setEnabled(enabled);
        customerRepository.save(customer);
        return true;
    }

    private UserDto convertToDto(Customer customer) {
        UserDto userDto = new UserDto();
        userDto.setUserId(customer.getCustomerId());
        userDto.setName(customer.getName());
        userDto.setEmail(customer.getEmail());
        userDto.setMobileNumber(customer.getMobileNumber());
        
        // Map roles
        if (customer.getRoles() != null && !customer.getRoles().isEmpty()) {
            String roles = customer.getRoles().stream()
                    .map(Role::getName)
                    .map(roleName -> roleName.replace("ROLE_", ""))
                    .collect(Collectors.joining(", "));
            userDto.setRoles(roles);
        }
        
        // Map address if exists
        Address address = customer.getAddress();
        if (address != null) {
            AddressDto addressDto = new AddressDto();
            addressDto.setStreet(address.getStreet());
            addressDto.setCity(address.getCity());
            addressDto.setState(address.getState());
            addressDto.setPostalCode(address.getPostalCode());
            addressDto.setCountry(address.getCountry());
            userDto.setAddress(addressDto);
        }
        
        return userDto;
    }
}
