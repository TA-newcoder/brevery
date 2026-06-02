package com.brevery.controller;

import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.UserAdminResponse;
import com.brevery.enums.Role;
import com.brevery.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User - Admin", description = "Quản lý thành viên, tìm kiếm bộ lọc, tổng chi tiêu và khóa/mở khóa tài khoản")
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    @Operation(summary = "Xem danh sách thành viên trong hệ thống (Có bộ lọc tìm kiếm, vai trò, trạng thái hoạt động và thống kê mua hàng)")
    public ResponseEntity<ApiResponse<Page<UserAdminResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserAdminResponse> users = adminService.getAllUsersForAdmin(search, role, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PatchMapping("/{userId}/toggle")
    @Operation(summary = "Khóa hoặc mở khóa tài khoản người dùng (Nếu khóa sẽ ép buộc logout lập tức)")
    public ResponseEntity<ApiResponse<Void>> toggleUserActiveState(@PathVariable Long userId) {
        adminService.toggleUserActiveState(userId);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái tài khoản thành công"));
    }
}
