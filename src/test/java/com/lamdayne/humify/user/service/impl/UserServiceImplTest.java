package com.lamdayne.humify.user.service.impl;

import com.lamdayne.humify.auth.repository.UserHasRoleRepository;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.auth.service.RoleAccessService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.user.dto.request.CreateUserRequest;
import com.lamdayne.humify.user.dto.response.UserResponse;
import com.lamdayne.humify.user.dto.response.UserRoleResponse;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.mapper.UserMapper;
import com.lamdayne.humify.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Test")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleAccessService roleAccessService;

    @Mock
    private CompanyAccessService companyAccessService;

    @Mock
    private UserHasRoleRepository userHasRoleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private MockedStatic<CompanyContext> companyContextMocked;
    private User user;
    private Company company;
    private CreateUserRequest createUserRequest;
    private UserResponse userResponse;
    private List<UserRoleResponse> userRoles;

    @BeforeEach
    void setUp() {
        this.companyContextMocked = Mockito.mockStatic(CompanyContext.class);

        this.company = new Company();

        this.user = User.builder()
                .company(company)
                .email("test@gmail.com")
                .password("password")
                .active(true)
                .build();
        this.user.setId(1L);

        this.createUserRequest = CreateUserRequest.builder()
                .email("test@gmail.com")
                .password("password")
                .roleIds(List.of(1L, 2L, 3L))
                .build();

        this.userResponse = UserResponse.builder()
                .id(1L)
                .email("test@gmail.com")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        this.userRoles = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        if (companyContextMocked != null && !companyContextMocked.isClosed()) {
            companyContextMocked.close();
        }
    }

    @Nested
    @DisplayName("exitsByEmail Test")
    class ExitsByEmail {
        @Test
        @DisplayName("Should return true when company ID exists and email exists")
        void existsByEmail_WhenCompanyIdExistsAndEmailExists_ShouldReturnTrue() {
            // given
            String email = "test@example.com";
            Long companyId = 1L;
            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(companyId);

            when(userRepository.existsByEmailAndCompanyId(email, companyId)).thenReturn(Boolean.TRUE);

            // when
            boolean result = userService.existsByEmail(email);

            // Then
            assertTrue(result);

            verify(userRepository, never()).existsByEmailAndCompanyIsNull(email);
            verify(userRepository, times(1)).existsByEmailAndCompanyId(email, companyId);

        }

        @Test
        @DisplayName("Should return true when company ID is null and system email exists")
        void existsByEmail_WhenCompanyIdIsNullAndSystemEmailExists_ShouldReturnTrue() {
            // Given
            String email = "test@gmail.com";

            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(null);

            when(userRepository.existsByEmailAndCompanyIsNull(email)).thenReturn(Boolean.TRUE);

            // when
            boolean result = userService.existsByEmail(email);

            // Then
            assertTrue(result);

            verify(userRepository).existsByEmailAndCompanyIsNull(email);

            verify(userRepository, never()).existsByEmailAndCompanyId(anyString(), anyLong());

        }
    }

    @Test
    @DisplayName("Should save user")
    void save_ShouldSaveUser() {
        userService.save(user);

        verify(userRepository, times(1)).save(user);
    }

    @Nested
    @DisplayName("findByEmail Test")
    class findByEmail {
        @Test
        @DisplayName("Should return user when company ID exists and user is found")
        void findByEmail_WhenCompanyIdExistsAndUserFound_ShouldReturnUser() {
            // Given
            String email = "test@gmail.com";
            Long companyId = 1L;

            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(companyId);

            when(userRepository.findByEmailAndCompanyId(email, companyId)).thenReturn(Optional.of(user));

            // When
            userService.findByEmail(email);

            // Then
            verify(userRepository, times(1)).findByEmailAndCompanyId(email, companyId);
            verify(userRepository, never()).findByEmailAndCompanyIsNull(email);
            verify(userRepository, never()).findAllByEmail(email);
        }

        @Test
        @DisplayName("Should throw USER_NOT_FOUND when company ID exists and user is not found")
        void findByEmail_WhenCompanyIdExistsAndUserNotFound_ShouldThrowUserNotFoundException() {
            // Given
            String email = "test@gmail.com";
            Long companyId = 1L;

            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(companyId);

            when(userRepository.findByEmailAndCompanyId(email, companyId)).thenReturn(Optional.empty());

            // When
            final AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.findByEmail(email)
            );

            // Then
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            verify(userRepository, times(1)).findByEmailAndCompanyId(email, companyId);
            verify(userRepository, never()).findByEmailAndCompanyIsNull(email);
            verify(userRepository, never()).findAllByEmail(email);

        }

        @Test
        @DisplayName("Should return system user when company ID is null and system user is found")
        void findByEmail_WhenCompanyIdIsNullAndSystemUserFound_ShouldReturnUser() {
            // Given
            String email = "test@gmail.com";

            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(null);
            when(userRepository.findByEmailAndCompanyIsNull(email)).thenReturn(Optional.of(user));

            // When
            final User result = userService.findByEmail(email);

            // Then
            assertSame(user, result);

            verify(userRepository, never()).findByEmailAndCompanyId(eq(email), anyLong());
            verify(userRepository, times(1)).findByEmailAndCompanyIsNull(email);
            verify(userRepository, never()).findAllByEmail(email);

        }

        @Test
        @DisplayName("Should throw USER_NOT_FOUND when company ID is null and no user is found")
        void findByEmail_WhenCompanyIdIsNullAndNoUserFound_ShouldThrowUserNotFoundException() {
            // Given
            String email = "test@gmail.com";

            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(null);

            when(userRepository.findByEmailAndCompanyIsNull(email)).thenReturn(Optional.empty());
            when(userRepository.findAllByEmail(email)).thenReturn(List.of());

            // When
            final AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.findByEmail(email)
            );

            // Then
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            verify(userRepository, never()).findByEmailAndCompanyId(eq(email), anyLong());
            verify(userRepository, times(1)).findByEmailAndCompanyIsNull(email);
            verify(userRepository, times(1)).findAllByEmail(email);

        }

        @Test
        @DisplayName("Should return user when company ID is null and exactly one user is found")
        void findByEmail_WhenCompanyIdIsNullAndOneUserFound_ShouldReturnUser() {
            // Given
            String email = "test@gmail.com";

            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(null);

            when(userRepository.findByEmailAndCompanyIsNull(email)).thenReturn(Optional.empty());
            when(userRepository.findAllByEmail(email)).thenReturn(List.of(user));

            // When
            final User result = userService.findByEmail(email);

            // Then
            assertSame(user, result);
            verify(userRepository, never()).findByEmailAndCompanyId(eq(email), anyLong());
            verify(userRepository, times(1)).findByEmailAndCompanyIsNull(email);
            verify(userRepository, times(1)).findAllByEmail(email);

        }

        @Test
        @DisplayName("Should throw USER_NOT_FOUND when company ID is null and multiple users are found")
        void findByEmail_WhenCompanyIdIsNullAndMultipleUsersFound_ShouldThrowUserNotFoundException() {
            // Given
            String email = "test@gmail.com";
            User user1 = new User();
            User user2 = new User();

            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(null);

            when(userRepository.findByEmailAndCompanyIsNull(email)).thenReturn(Optional.empty());
            when(userRepository.findAllByEmail(email)).thenReturn(List.of(user1, user2));

            // When
            final AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.findByEmail(email)
            );

            // Then
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            verify(userRepository, never()).findByEmailAndCompanyId(eq(email), anyLong());
            verify(userRepository, times(1)).findByEmailAndCompanyIsNull(email);
            verify(userRepository, times(1)).findAllByEmail(email);

        }
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {

    }

}
