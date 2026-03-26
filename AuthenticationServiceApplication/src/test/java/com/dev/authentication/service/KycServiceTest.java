package com.dev.authentication.service;

import com.dev.authentication.dto.KycRequest;
import com.dev.authentication.dto.KycResponse;
import com.dev.authentication.entity.Kyc;
import com.dev.authentication.entity.KycStatus;
import com.dev.authentication.entity.User;
import com.dev.authentication.exception.EntityNotFoundException;
import com.dev.authentication.mapper.KycMapper;
import com.dev.authentication.repository.KycRepository;
import com.dev.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

    @Mock private KycRepository repo;
    @Mock private UserRepository userRepo;
    @Mock private KycMapper mapper;

    @InjectMocks private KycService service;

    private User sampleUser;
    private Kyc sampleKyc;
    private KycRequest validRequest;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // Set up dummy upload dir in a temp folder
        ReflectionTestUtils.setField(service, "UPLOAD_DIR", System.getProperty("java.io.tmpdir") + "/kyc_uploads");

        sampleUser = User.builder()
                .id(1L)
                .email("john@example.com")
                .build();

        sampleKyc = Kyc.builder()
                .id(1L)
                .contactNumber("1234567890")
                .documentType("Passport")
                .documentPath("path/to/doc.pdf")
                .address("123 Main St")
                .status(KycStatus.PENDING)
                .user(sampleUser)
                .build();

        validRequest = new KycRequest();
        validRequest.setContactNumber("1234567890");
        validRequest.setAddress("123 Main St");
        validRequest.setDocumentType("Passport");

        mockFile = new MockMultipartFile("file", "doc.pdf", "application/pdf", "dummy content".getBytes());
    }

    @Nested
    @DisplayName("upload()")
    class Upload {
        @Test
        @DisplayName("should upload file and save KYC")
        void upload_success() throws Exception {
            when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
            when(repo.save(any(Kyc.class))).thenReturn(sampleKyc);

            service.upload("john@example.com", validRequest, mockFile);

            verify(repo).save(any(Kyc.class));
            
            // Clean up the created file
            File dir = new File(System.getProperty("java.io.tmpdir") + "/kyc_uploads");
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
                dir.delete();
            }
        }

        @Test
        @DisplayName("should throw error if user not found")
        void upload_userNotFound() {
            when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.upload("john@example.com", validRequest, mockFile))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User");

            verify(repo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getByEmail()")
    class GetByEmail {
        @Test
        @DisplayName("should return KYC response")
        void getByEmail_success() {
            when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
            when(repo.findByUserId(1L)).thenReturn(Optional.of(sampleKyc));
            KycResponse mockResponse = KycResponse.builder()
                    .id(1L).contactNumber("1234567890").address("123 Main St")
                    .documentType("Passport").status(KycStatus.PENDING)
                    .userEmail("john@example.com").build();
            when(mapper.toResponse(sampleKyc)).thenReturn(mockResponse);

            KycResponse result = service.getByEmail("john@example.com");

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(KycStatus.PENDING);
        }

        @Test
        @DisplayName("should throw error if KYC not found")
        void getByEmail_kycNotFound() {
            when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
            when(repo.findByUserId(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getByEmail("john@example.com"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("KYC");
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {
        @Test
        @DisplayName("should return paginated KYC responses")
        void getAll_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Kyc> page = new PageImpl<>(List.of(sampleKyc));
            when(repo.findAll(pageable)).thenReturn(page);
            KycResponse mockResponse = KycResponse.builder()
                    .id(1L).contactNumber("1234567890").address("123 Main St")
                    .documentType("Passport").status(KycStatus.PENDING)
                    .userEmail("john@example.com").build();
            when(mapper.toResponse(sampleKyc)).thenReturn(mockResponse);

            Page<KycResponse> result = service.getAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(mockResponse);
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {
        @Test
        @DisplayName("should update KYC status")
        void updateStatus_success() {
            when(repo.findById(1L)).thenReturn(Optional.of(sampleKyc));
            when(repo.save(any(Kyc.class))).thenReturn(sampleKyc);

            service.updateStatus(1L, KycStatus.APPROVED);

            assertThat(sampleKyc.getStatus()).isEqualTo(KycStatus.APPROVED);
            verify(repo).save(sampleKyc);
        }

        @Test
        @DisplayName("should throw error if KYC not found")
        void updateStatus_notFound() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStatus(99L, KycStatus.APPROVED))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("KYC");
        }
    }
}
