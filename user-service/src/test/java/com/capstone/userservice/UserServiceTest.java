//package com.capstone.userservice;
//
//import com.capstone.userservice.dto.AddressDto;
//import com.capstone.userservice.dto.UserRequest;
//import com.capstone.userservice.dto.UserResponse;
//import com.capstone.userservice.exceptions.DuplicateUserException;
//import com.capstone.userservice.exceptions.UserNotFoundException;
//import com.capstone.userservice.model.Address;
//import com.capstone.userservice.model.User;
//import com.capstone.userservice.repository.UserRepository;
//import com.capstone.userservice.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private WebClient.Builder webClientBuilder;
//
//    @Mock
//    private WebClient webClient;
//
//    @InjectMocks
//    private UserService userService;
//
//    private AddressDto testAddressDto;
//
//    @BeforeEach
//    void setUp() {
//        testAddressDto = AddressDto.builder()
//                .number("123")
//                .street("Main St")
//                .city("New York")
//                .stateCode("NY")
//                .zip("10001")
//                .countryCode("US")
//                .build();
//    }
//
//    @Test
//    void shouldCreateUserSuccessfully() {
//        // Arrange
//        UserRequest userRequest = UserRequest.builder()
//                .username("testuser")
//                .password("password123")
//                .email("test@example.com")
//                .firstName("John")
//                .lastName("Doe")
//                .address(testAddressDto)
//                .build();
//
//        Address address = new Address(testAddressDto);
//        address.setLocationId("test-location-id");
//
//        User savedUser = User.builder()
//                .userId(1L)
//                .username("testuser")
//                .password("password123")
//                .email("test@example.com")
//                .firstName("John")
//                .lastName("Doe")
//                .address(address)
//                .favoriteLocationIds(new HashSet<>())
//                .build();
//
//        // Mock WebClient for getLocationId() call
//        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
//        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
//
//        when(webClientBuilder.build()).thenReturn(webClient);
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        // Fix: Use any() instead of specific matcher
//        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("test-location-id"));
//
//        when(userRepository.existsByUsername("testuser")).thenReturn(false);
//        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
//        when(userRepository.save(any(User.class))).thenReturn(savedUser);
//
//        // Act
//        UserResponse result = userService.createUser(userRequest);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1L, result.getUserId());
//        assertEquals("testuser", result.getUsername());
//        assertEquals("test@example.com", result.getEmail());
//
//        verify(userRepository).existsByUsername("testuser");
//        verify(userRepository).existsByEmail("test@example.com");
//        verify(userRepository).save(any(User.class));
//        verify(webClientBuilder).build();
//    }
//
//    @Test
//    void shouldThrowDuplicateUserExceptionWhenUsernameExists() {
//        // Arrange
//        UserRequest userRequest = UserRequest.builder()
//                .username("existinguser")
//                .password("password123")
//                .email("test@example.com")
//                .firstName("John")
//                .lastName("Doe")
//                .address(testAddressDto)
//                .build();
//
//        when(userRepository.existsByUsername("existinguser")).thenReturn(true);
//
//        // Act & Assert
//        assertThrows(DuplicateUserException.class, () -> {
//            userService.createUser(userRequest);
//        });
//
//        verify(userRepository).existsByUsername("existinguser");
//        verify(userRepository, never()).existsByEmail(anyString());
//        verify(userRepository, never()).save(any(User.class));
//        verifyNoInteractions(webClientBuilder);  // Should not call WebClient if duplicate
//    }
//
//    @Test
//    void shouldGetUserSuccessfully() {
//        // Arrange
//        Long userId = 1L;
//        Address address = new Address(testAddressDto);
//        User user = User.builder()
//                .userId(userId)
//                .username("testuser")
//                .password("password123")
//                .email("test@example.com")
//                .firstName("John")
//                .lastName("Doe")
//                .address(address)
//                .favoriteLocationIds(new HashSet<>())
//                .build();
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//
//        // Act
//        UserResponse result = userService.getUser(userId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(userId, result.getUserId());
//        assertEquals("testuser", result.getUsername());
//
//        verify(userRepository).findById(userId);
//        verifyNoInteractions(webClientBuilder);  // No WebClient calls for getUser
//    }
//
//    @Test
//    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
//        // Arrange
//        Long userId = 999L;
//        when(userRepository.findById(userId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(UserNotFoundException.class, () -> {
//            userService.getUser(userId);
//        });
//
//        verify(userRepository).findById(userId);
//        verifyNoInteractions(webClientBuilder);
//    }
//
//    @Test
//    void shouldUpdateUserSuccessfully() {
//        // Arrange
//        Long userId = 1L;
//        Address address = new Address(testAddressDto);
//        User existingUser = User.builder()
//                .userId(userId)
//                .username("olduser")
//                .password("oldpass")
//                .email("old@example.com")
//                .firstName("Old")
//                .lastName("Name")
//                .address(address)
//                .favoriteLocationIds(new HashSet<>())
//                .build();
//
//        Map<String, Object> updates = Map.of(
//                "firstName", "NewFirstName",
//                "lastName", "NewLastName",
//                "email", "new@example.com"
//        );
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
//        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
//        when(userRepository.save(any(User.class))).thenReturn(existingUser);
//
//        // Act
//        UserResponse result = userService.updateUser(userId, updates);
//
//        // Assert
//        assertNotNull(result);
//        verify(userRepository).findById(userId);
//        verify(userRepository).existsByEmail("new@example.com");
//        verify(userRepository).save(existingUser);
//        verifyNoInteractions(webClientBuilder);  // No WebClient calls for update without address change
//    }
//
//    @Test
//    void shouldDeleteUserSuccessfully() {
//        // Arrange
//        Long userId = 1L;
//        Address address = new Address(testAddressDto);
//        User existingUser = User.builder()
//                .userId(userId)
//                .username("testuser")
//                .password("password")
//                .email("test@example.com")
//                .firstName("Test")
//                .lastName("User")
//                .address(address)
//                .favoriteLocationIds(new HashSet<>())
//                .build();
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
//        doNothing().when(userRepository).deleteById(userId);
//
//        // Act
//        userService.deleteUser(userId);
//
//        // Assert
//        verify(userRepository).findById(userId);
//        verify(userRepository).deleteById(userId);
//        verifyNoInteractions(webClientBuilder);  // No WebClient calls for delete
//    }
//
//    @Test
//    void shouldUpdateUserWithAddressChanges() {
//        // Arrange
//        Long userId = 1L;
//        Address address = new Address(testAddressDto);
//        User existingUser = User.builder()
//                .userId(userId)
//                .username("olduser")
//                .password("oldpass")
//                .email("old@example.com")
//                .firstName("Old")
//                .lastName("Name")
//                .address(address)
//                .favoriteLocationIds(new HashSet<>())
//                .build();
//
//        Map<String, Object> addressUpdates = Map.of(
//                "city", "Los Angeles",
//                "stateCode", "CA",
//                "countryCode", "US"
//        );
//
//        Map<String, Object> updates = Map.of("address", addressUpdates);
//
//        // Mock WebClient for getLocationId() call triggered by address update
//        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
//        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
//
//        when(webClientBuilder.build()).thenReturn(webClient);
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        // Fix: Use any() instead of specific matcher
//        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("new-location-id"));
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
//        when(userRepository.save(any(User.class))).thenReturn(existingUser);
//
//        // Act
//        UserResponse result = userService.updateUser(userId, updates);
//
//        // Assert
//        assertNotNull(result);
//        verify(userRepository).findById(userId);
//        verify(userRepository).save(existingUser);
//        verify(webClientBuilder).build();  // WebClient should be called for address update
//    }
//
//    @Test
//    void shouldAddFavoriteLocationSuccessfully() {
//        // Arrange
//        Long userId = 1L;
//        String locationId = "test-location-123";
//        Address address = new Address(testAddressDto);
//        User user = User.builder()
//                .userId(userId)
//                .username("testuser")
//                .password("password")
//                .email("test@example.com")
//                .firstName("Test")
//                .lastName("User")
//                .address(address)
//                .favoriteLocationIds(new HashSet<>())  // Initialize the Set
//                .build();
//
//        // Mock WebClient for location verification
//        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
//        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
//
//        when(webClientBuilder.build()).thenReturn(webClient);
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        // Fix: Use anyString() instead of any(String.class)
//        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        // Act
//        userService.addFavoriteLocation(userId, locationId);
//
//        // Assert
//        verify(userRepository, times(2)).findById(userId);  // Called twice in the service method
//        verify(userRepository).save(user);
//        verify(webClientBuilder).build();
//        verify(webClient).get();
//    }
//
//    @Test
//    void shouldRemoveFavoriteLocationSuccessfully() {
//        // Arrange
//        Long userId = 1L;
//        String locationId = "location-to-remove";
//        Address address = new Address(testAddressDto);
//        Set<String> favorites = new HashSet<>();
//        favorites.add(locationId);
//
//        User user = User.builder()
//                .userId(userId)
//                .username("testuser")
//                .password("password")
//                .email("test@example.com")
//                .firstName("Test")
//                .lastName("User")
//                .address(address)
//                .favoriteLocationIds(favorites)
//                .build();
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        // Act
//        userService.removeFavoriteLocation(userId, locationId);
//
//        // Assert
//        verify(userRepository).findById(userId);
//        verify(userRepository).save(user);
//        verifyNoInteractions(webClientBuilder);  // No WebClient calls for removeFavorite
//    }
//
//    @Test
//    void shouldCheckIfLocationIsFavorited() {
//        // Arrange
//        Long userId = 1L;
//        String locationId = "existing-location";
//        Address address = new Address(testAddressDto);
//        Set<String> favorites = new HashSet<>();
//        favorites.add(locationId);
//
//        User user = User.builder()
//                .userId(userId)
//                .username("testuser")
//                .password("password")
//                .email("test@example.com")
//                .firstName("Test")
//                .lastName("User")
//                .address(address)
//                .favoriteLocationIds(favorites)
//                .build();
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//
//        // Act
//        boolean result = userService.isLocationFavorited(userId, locationId);
//
//        // Assert
//        assertTrue(result);
//        verify(userRepository).findById(userId);
//        verifyNoInteractions(webClientBuilder);
//    }
//
//    @Test
//    void shouldReturnFalseWhenLocationNotFavorited() {
//        // Arrange
//        Long userId = 1L;
//        String locationId = "non-existent-location";
//        Address address = new Address(testAddressDto);
//        Set<String> favorites = new HashSet<>();
//        favorites.add("different-location");
//
//        User user = User.builder()
//                .userId(userId)
//                .username("testuser")
//                .password("password")
//                .email("test@example.com")
//                .firstName("Test")
//                .lastName("User")
//                .address(address)
//                .favoriteLocationIds(favorites)
//                .build();
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//
//        // Act
//        boolean result = userService.isLocationFavorited(userId, locationId);
//
//        // Assert
//        assertFalse(result);
//        verify(userRepository).findById(userId);
//        verifyNoInteractions(webClientBuilder);
//    }
//
//    @Test
//    void shouldGetFavoriteLocations() {
//        // Arrange
//        Long userId = 1L;
//        Address address = new Address(testAddressDto);
//        Set<String> favorites = new HashSet<>();
//        favorites.add("loc1");
//        favorites.add("loc2");
//
//        User user = User.builder()
//                .userId(userId)
//                .username("testuser")
//                .password("password")
//                .email("test@example.com")
//                .firstName("Test")
//                .lastName("User")
//                .address(address)
//                .favoriteLocationIds(favorites)
//                .build();
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//
//        // Act
//        Set<String> result = userService.getFavoriteLocations(userId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertTrue(result.contains("loc1"));
//        assertTrue(result.contains("loc2"));
//        verify(userRepository).findById(userId);
//        verifyNoInteractions(webClientBuilder);
//    }
//}