package com.lamdayne.humify.user.service.impl;

import com.lamdayne.humify.auth.entity.Role;
import com.lamdayne.humify.auth.entity.UserHasRole;
import com.lamdayne.humify.auth.enums.SystemRole;
import com.lamdayne.humify.auth.repository.UserHasRoleRepository;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.auth.service.RoleAccessService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.user.dto.request.ChangePasswordRequest;
import com.lamdayne.humify.user.dto.request.ChangeRoleRequest;
import com.lamdayne.humify.user.dto.request.CreateUserRequest;
import com.lamdayne.humify.user.dto.response.UserResponse;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.enums.PasswordFlag;
import com.lamdayne.humify.user.mapper.UserMapper;
import com.lamdayne.humify.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @BeforeEach
    void setUp() {
        this.companyContextMocked = Mockito.mockStatic(CompanyContext.class);

        this.company = new Company();
        this.company.setId(1L);

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
        @DisplayName("Should return false when company ID exists and email does not exist")
        void existsByEmail_WhenCompanyIdExistsAndEmailNotExists_ShouldReturnFalse() {
            // given
            String email = "test@example.com";
            Long companyId = 1L;
            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(companyId);

            when(userRepository.existsByEmailAndCompanyId(email, companyId)).thenReturn(Boolean.FALSE);

            // when
            boolean result = userService.existsByEmail(email);

            // Then
            assertFalse(result);
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

        @Test
        @DisplayName("Should return false when company ID is null and system email does not exist")
        void existsByEmail_WhenCompanyIdIsNullAndSystemEmailNotExists_ShouldReturnFalse() {
            // Given
            String email = "test@gmail.com";

            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(null);

            when(userRepository.existsByEmailAndCompanyIsNull(email)).thenReturn(Boolean.FALSE);

            // when
            boolean result = userService.existsByEmail(email);

            // Then
            assertFalse(result);
            verify(userRepository).existsByEmailAndCompanyIsNull(email);
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
    class FindByEmail {
        @Test
        @DisplayName("Should return user when company ID exists and user is found")
        void findByEmail_WhenCompanyIdExistsAndUserFound_ShouldReturnUser() {
            // Given
            String email = "test@gmail.com";
            Long companyId = 1L;

            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(companyId);

            when(userRepository.findByEmailAndCompanyId(email, companyId)).thenReturn(Optional.of(user));

            // When
            User result = userService.findByEmail(email);

            // Then
            assertSame(user, result);
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
    @DisplayName("create Test")
    class CreateUserTests {
        @Test
        @DisplayName("Should throw USER_EMAIL_EXISTED when company ID exists and email exists")
        void create_WhenCompanyIdExistsAndEmailExists_ShouldThrowUserEmailExistedException() {
            // Given
            Long companyId = 1L;
            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(companyId);
            when(userRepository.existsByEmailAndCompanyId(createUserRequest.getEmail(), companyId)).thenReturn(true);

            // When
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.create(createUserRequest)
            );

            // Then
            assertEquals(ErrorCode.USER_EMAIL_EXISTED, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw USER_EMAIL_EXISTED when company ID is null and email exists")
        void create_WhenCompanyIdIsNullAndEmailExists_ShouldThrowUserEmailExistedException() {
            // Given
            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(null);
            when(userRepository.existsByEmailAndCompanyIsNull(createUserRequest.getEmail())).thenReturn(true);

            // When
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.create(createUserRequest)
            );

            // Then
            assertEquals(ErrorCode.USER_EMAIL_EXISTED, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should create user successfully when company ID exists and email does not exist")
        void create_WhenCompanyIdExistsAndEmailNotExists_ShouldCreateUser() {
            // Given
            Long companyId = 1L;
            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(companyId);
            when(userRepository.existsByEmailAndCompanyId(createUserRequest.getEmail(), companyId)).thenReturn(false);
            when(userMapper.toUser(createUserRequest)).thenReturn(user);
            when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
            when(companyAccessService.getReferenceById(companyId)).thenReturn(company);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            Role role1 = Role.builder().name("ROLE_USER").build();
            role1.setId(1L);
            Role role2 = Role.builder().name("ROLE_ADMIN").build();
            role2.setId(2L);

            UserHasRole uhr1 = UserHasRole.builder().user(user).role(role1).build();
            UserHasRole uhr2 = UserHasRole.builder().user(user).role(role2).build();
            when(userHasRoleRepository.findAllByUserIdIn(List.of(user.getId()))).thenReturn(List.of(uhr1, uhr2));

            // When
            UserResponse response = userService.create(createUserRequest);

            // Then
            assertNotNull(response);
            assertEquals(2, response.getRoles().size());
            assertEquals("ROLE_ADMIN", response.getRoles().get(0).getName());
            assertEquals("ROLE_USER", response.getRoles().get(1).getName());

            verify(roleAccessService, times(1)).assignRoles(user, createUserRequest.getRoleIds());
            verify(userRepository, times(1)).save(user);
        }

        @Test
        @DisplayName("Should create user successfully when company ID is null and email does not exist")
        void create_WhenCompanyIdIsNullAndEmailNotExists_ShouldCreateUser() {
            // Given
            companyContextMocked.when(CompanyContext::getCompanyId).thenReturn(null);
            when(userRepository.existsByEmailAndCompanyIsNull(createUserRequest.getEmail())).thenReturn(false);
            when(userMapper.toUser(createUserRequest)).thenReturn(user);
            when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
            when(companyAccessService.getReferenceById(null)).thenReturn(null);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);
            when(userHasRoleRepository.findAllByUserIdIn(List.of(user.getId()))).thenReturn(List.of());

            // When
            UserResponse response = userService.create(createUserRequest);

            // Then
            assertNotNull(response);
            assertTrue(response.getRoles().isEmpty());
            verify(userRepository, times(1)).save(user);
        }
    }

    @Nested
    @DisplayName("findAll Test")
    class FindAllTest {
        @Test
        @DisplayName("Should return PageResponse with filtered users and roles")
        void findAll_WhenUsersExist_ShouldReturnPageResponseWithFilteredUsersAndRoles() {
            // Given
            int page = 0;
            int size = 10;

            User user1 = User.builder().company(company).build();
            user1.setId(1L);

            User userWithoutCompany = User.builder().company(null).build();
            userWithoutCompany.setId(2L);

            List<User> userList = List.of(user1, userWithoutCompany);
            Page<User> userPage = new PageImpl<>(userList, PageRequest.of(page, size), userList.size());

            when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
            when(userMapper.toResponse(user1)).thenReturn(userResponse);

            Role role = Role.builder().name("ADMIN").build();
            role.setId(10L);
            UserHasRole uhr = UserHasRole.builder().user(user1).role(role).build();
            when(userHasRoleRepository.findAllByUserIdIn(List.of(1L))).thenReturn(List.of(uhr));

            // When
            PageResponse<UserResponse> result = userService.findAll(page, size);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getItems().size());
            assertEquals(1, result.getItems().get(0).getRoles().size());
            assertEquals("ADMIN", result.getItems().get(0).getRoles().get(0).getName());
        }

        @Test
        @DisplayName("Should return empty PageResponse when all users are filtered out or empty")
        void findAll_WhenNoUsersMatchFilter_ShouldReturnEmptyPageResponse() {
            // Given
            int page = 0;
            int size = 10;
            User userWithoutCompany = User.builder().company(null).build();
            userWithoutCompany.setId(2L);

            Page<User> userPage = new PageImpl<>(List.of(userWithoutCompany), PageRequest.of(page, size), 1);
            when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

            // When
            PageResponse<UserResponse> result = userService.findAll(page, size);

            // Then
            assertNotNull(result);
            assertTrue(result.getItems().isEmpty());
            verify(userHasRoleRepository, never()).findAllByUserIdIn(anyList());
        }
    }

    @Nested
    @DisplayName("changeRole Test")
    class ChangeRoleTest {
        @Test
        @DisplayName("Should change user role when user exists")
        void changeRole_WhenUserExists_ShouldAssignRoles() {
            // Given
            Long userId = 1L;
            ChangeRoleRequest request = new ChangeRoleRequest();
            request.setRoleIds(List.of(1L, 2L));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // When
            userService.changeRole(userId, request);

            // Then
            verify(roleAccessService, times(1)).assignRoles(user, request.getRoleIds());
        }

        @Test
        @DisplayName("Should throw USER_NOT_FOUND when user does not exist")
        void changeRole_WhenUserNotFound_ShouldThrowUserNotFoundException() {
            // Given
            Long userId = 1L;
            ChangeRoleRequest request = new ChangeRoleRequest();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.changeRole(userId, request)
            );
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            verify(roleAccessService, never()).assignRoles(any(), any());
        }
    }

    @Nested
    @DisplayName("findById Test")
    class FindByIdTest {
        @Test
        @DisplayName("Should throw USER_NOT_FOUND when user does not exist")
        void findById_WhenUserNotFound_ShouldThrowUserNotFoundException() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.findById(userId)
            );
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw USER_NOT_FOUND when user company is null")
        void findById_WhenUserCompanyIsNull_ShouldThrowUserNotFoundException() {
            // Given
            Long userId = 1L;
            User userNoCompany = User.builder().company(null).build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(userNoCompany));

            // When & Then
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.findById(userId)
            );
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should return UserResponse with roles when user exists with company")
        void findById_WhenUserCompanyExists_ShouldReturnUserResponseWithRoles() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            Role role = Role.builder().name("MANAGER").build();
            role.setId(5L);
            UserHasRole uhr = UserHasRole.builder().user(user).role(role).build();
            when(userHasRoleRepository.findAllByUserIdIn(List.of(userId))).thenReturn(List.of(uhr));

            // When
            UserResponse result = userService.findById(userId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getRoles().size());
            assertEquals("MANAGER", result.getRoles().get(0).getName());
        }
    }

    @Nested
    @DisplayName("changePassword Test")
    class ChangePasswordTest {
        private ChangePasswordRequest request;
        private UserPrincipal selfPrincipal;

        @BeforeEach
        void setUpPasswordRequest() {
            request = new ChangePasswordRequest();
            request.setOldPassword("oldPass");
            request.setNewPassword("newPass");

            selfPrincipal = UserPrincipal.builder()
                    .id(1L)
                    .build();
        }

        @Test
        @DisplayName("Should throw USER_NOT_FOUND when user does not exist")
        void changePassword_WhenUserNotFound_ShouldThrowUserNotFoundException() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.changePassword(userId, request, selfPrincipal)
            );
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw ACCESS_DENIED when user is not self and does not have admin/hr roles")
        void changePassword_WhenUserNotSelfAndNotAdminOrHr_ShouldThrowAccessDeniedException() {
            // Given
            Long userId = 1L;
            UserPrincipal otherUserPrincipal = UserPrincipal.builder().id(99L).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(roleAccessService.findAllRoleNames(otherUserPrincipal)).thenReturn(Set.of("EMPLOYEE"));

            // When & Then
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.changePassword(userId, request, otherUserPrincipal)
            );
            assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw USER_PASSWORD_NOT_MATCH when old password is wrong")
        void changePassword_WhenOldPasswordDoesNotMatch_ShouldThrowUserPasswordNotMatchException() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(roleAccessService.findAllRoleNames(selfPrincipal)).thenReturn(Set.of("EMPLOYEE"));
            when(passwordEncoder.matches("oldPass", user.getPassword())).thenReturn(false);

            // When & Then
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.changePassword(userId, request, selfPrincipal)
            );
            assertEquals(ErrorCode.USER_PASSWORD_NOT_MATCH, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should change password when user is self and old password matches")
        void changePassword_WhenIsSelfAndOldPasswordMatches_ShouldChangePassword() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(roleAccessService.findAllRoleNames(selfPrincipal)).thenReturn(Set.of("EMPLOYEE"));
            when(passwordEncoder.matches("oldPass", user.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

            // When
            userService.changePassword(userId, request, selfPrincipal);

            // Then
            assertEquals("encodedNewPass", user.getPassword());
            verify(userRepository, times(1)).save(user);
        }

        @Test
        @DisplayName("Should change password when user is company admin and old password matches")
        void changePassword_WhenIsAdminAndOldPasswordMatches_ShouldChangePassword() {
            // Given
            Long userId = 1L;
            UserPrincipal adminPrincipal = UserPrincipal.builder().id(99L).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(roleAccessService.findAllRoleNames(adminPrincipal)).thenReturn(Set.of(SystemRole.COMPANY_ADMIN.name()));
            when(passwordEncoder.matches("oldPass", user.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

            // When
            userService.changePassword(userId, request, adminPrincipal);

            // Then
            assertEquals("encodedNewPass", user.getPassword());
            verify(userRepository, times(1)).save(user);
        }

        @Test
        @DisplayName("Should change password when user is hr manager and old password matches")
        void changePassword_WhenIsHrManagerAndOldPasswordMatches_ShouldChangePassword() {
            // Given
            Long userId = 1L;
            UserPrincipal hrPrincipal = UserPrincipal.builder().id(88L).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(roleAccessService.findAllRoleNames(hrPrincipal)).thenReturn(Set.of(SystemRole.HR_MANAGER.name()));
            when(passwordEncoder.matches("oldPass", user.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

            // When
            userService.changePassword(userId, request, hrPrincipal);

            // Then
            assertEquals("encodedNewPass", user.getPassword());
            verify(userRepository, times(1)).save(user);
        }
    }

    @Nested
    @DisplayName("resetPassword Test")
    class ResetPasswordTest {
        @Test
        @DisplayName("Should throw USER_NOT_FOUND when user does not exist")
        void resetPassword_WhenUserNotFound_ShouldThrowUserNotFoundException() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.resetPassword(userId, "newSecret")
            );
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw USER_NOT_ACTIVATED when user is inactive and password is not pending activation")
        void resetPassword_WhenUserNotActiveAndPasswordNotPendingActivation_ShouldThrowUserNotActivatedException() {
            // Given
            Long userId = 1L;
            User inactiveUser = User.builder()
                    .active(false)
                    .password("somePassword")
                    .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));

            // When & Then
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.resetPassword(userId, "newSecret")
            );
            assertEquals(ErrorCode.USER_NOT_ACTIVATED, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should activate user and reset password when user password is PENDING_ACTIVATION")
        void resetPassword_WhenUserPendingActivation_ShouldActivateAndResetPassword() {
            // Given
            Long userId = 1L;
            User pendingUser = User.builder()
                    .active(false)
                    .password(PasswordFlag.PENDING_ACTIVATION.name())
                    .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(pendingUser));
            when(passwordEncoder.encode("newSecret")).thenReturn("encodedSecret");

            // When
            userService.resetPassword(userId, "newSecret");

            // Then
            assertTrue(pendingUser.getActive());
            assertEquals("encodedSecret", pendingUser.getPassword());
            verify(userRepository, times(1)).save(pendingUser);
        }

        @Test
        @DisplayName("Should reset password when user is active")
        void resetPassword_WhenUserActive_ShouldResetPassword() {
            // Given
            Long userId = 1L;
            User activeUser = User.builder()
                    .active(true)
                    .password("oldEncrypted")
                    .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.encode("newSecret")).thenReturn("encodedSecret");

            // When
            userService.resetPassword(userId, "newSecret");

            // Then
            assertEquals("encodedSecret", activeUser.getPassword());
            verify(userRepository, times(1)).save(activeUser);
        }
    }

    @Test
    @DisplayName("Should return user optional for findByEmailAndCompanyId")
    void findByEmailAndCompanyId_ShouldReturnUserOptional() {
        // Given
        String email = "test@example.com";
        Long companyId = 10L;
        when(userRepository.findByEmailAndCompanyId(email, companyId)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByEmailAndCompanyId(email, companyId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    @DisplayName("Should create user with null password and active state")
    void createUser_ShouldSaveAndReturnUser() {
        // Given
        String email = "newuser@example.com";
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.createUser(email, company);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(company, result.getCompany());
        assertNull(result.getPassword());
        assertTrue(result.getActive());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Nested
    @DisplayName("getUserById Test")
    class GetUserByIdTest {
        @Test
        @DisplayName("Should return user when user exists")
        void getUserById_WhenUserExists_ShouldReturnUser() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // When
            User result = userService.getUserById(userId);

            // Then
            assertEquals(user, result);
        }

        @Test
        @DisplayName("Should throw USER_NOT_FOUND when user does not exist")
        void getUserById_WhenUserNotFound_ShouldThrowUserNotFoundException() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            AppException exception = assertThrows(
                    AppException.class,
                    () -> userService.getUserById(userId)
            );
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("getCurrentEmployeeId Test")
    class GetCurrentEmployeeIdTest {
        @Test
        @DisplayName("Should return employee ID when user and employee exist")
        void getCurrentEmployeeId_WhenUserAndEmployeeExist_ShouldReturnEmployeeId() {
            // Given
            Long userId = 1L;
            Long employeeId = 55L;

            Employee employee = new Employee();
            employee.setId(employeeId);

            User userWithEmployee = User.builder()
                    .employee(employee)
                    .build();
            userWithEmployee.setId(userId);

            UserPrincipal principal = UserPrincipal.builder().id(userId).build();

            try (MockedStatic<SecurityContextHolder> secContextMocked = Mockito.mockStatic(SecurityContextHolder.class)) {
                SecurityContext securityContext = mock(SecurityContext.class);
                Authentication authentication = mock(Authentication.class);

                secContextMocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.getPrincipal()).thenReturn(principal);
                when(userRepository.findById(userId)).thenReturn(Optional.of(userWithEmployee));

                // When
                Long result = userService.getCurrentEmployeeId();

                // Then
                assertEquals(employeeId, result);
            }
        }

        @Test
        @DisplayName("Should throw USER_NOT_FOUND when current user not found in repository")
        void getCurrentEmployeeId_WhenUserNotFound_ShouldThrowUserNotFoundException() {
            // Given
            Long userId = 1L;
            UserPrincipal principal = UserPrincipal.builder().id(userId).build();

            try (MockedStatic<SecurityContextHolder> secContextMocked = Mockito.mockStatic(SecurityContextHolder.class)) {
                SecurityContext securityContext = mock(SecurityContext.class);
                Authentication authentication = mock(Authentication.class);

                secContextMocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.getPrincipal()).thenReturn(principal);
                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                // When & Then
                AppException exception = assertThrows(
                        AppException.class,
                        () -> userService.getCurrentEmployeeId()
                );
                assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            }
        }

        @Test
        @DisplayName("Should throw EMPLOYEE_NOT_FOUND when user does not have employee")
        void getCurrentEmployeeId_WhenEmployeeNotFound_ShouldThrowEmployeeNotFoundException() {
            // Given
            Long userId = 1L;
            User userWithoutEmployee = User.builder()
                    .employee(null)
                    .build();
            userWithoutEmployee.setId(userId);

            UserPrincipal principal = UserPrincipal.builder().id(userId).build();

            try (MockedStatic<SecurityContextHolder> secContextMocked = Mockito.mockStatic(SecurityContextHolder.class)) {
                SecurityContext securityContext = mock(SecurityContext.class);
                Authentication authentication = mock(Authentication.class);

                secContextMocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.getPrincipal()).thenReturn(principal);
                when(userRepository.findById(userId)).thenReturn(Optional.of(userWithoutEmployee));

                // When & Then
                AppException exception = assertThrows(
                        AppException.class,
                        () -> userService.getCurrentEmployeeId()
                );
                assertEquals(ErrorCode.EMPLOYEE_NOT_FOUND, exception.getErrorCode());
            }
        }
    }

    @Test
    @DisplayName("Should save user and assign employee role")
    void createEmployeeUser_ShouldSaveUserAndAssignRole() {
        // Given
        when(userRepository.save(user)).thenReturn(user);

        // When
        User result = userService.createEmployeeUser(user);

        // Then
        assertEquals(user, result);
        verify(userRepository, times(1)).save(user);
        verify(roleAccessService, times(1)).assignEmployeeRole(user);
    }
}

