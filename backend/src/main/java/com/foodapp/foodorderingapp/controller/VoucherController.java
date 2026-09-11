package com.foodapp.foodorderingapp.controller;

import com.foodapp.foodorderingapp.dto.user_voucher.UserVoucherRequest;
import com.foodapp.foodorderingapp.dto.user_voucher.UserVoucherResponse;
import com.foodapp.foodorderingapp.entity.Voucher;
import com.foodapp.foodorderingapp.enumeration.VoucherStatus;
import com.foodapp.foodorderingapp.service.user_voucher.UserVoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final UserVoucherService userVoucherService;
    @PostMapping
    public ResponseEntity<Voucher> receiveVoucher(@Valid @RequestBody UserVoucherRequest userVoucherRequest) {
        return ResponseEntity.ok(userVoucherService.receiveVoucher(userVoucherRequest));
    }
    @GetMapping("/apply/{voucherId}")
    public ResponseEntity<UserVoucherResponse> tryApplyVoucher(@PathVariable Long voucherId) {
        return ResponseEntity.ok(userVoucherService.tryApplyVoucher(voucherId));}
    @GetMapping()
    public ResponseEntity<List<Voucher>> tryApplyVoucher(@RequestParam VoucherStatus status
                                                               ) {
        return ResponseEntity.ok(userVoucherService.findVouchersByStatusAndUserId(status));}
}
