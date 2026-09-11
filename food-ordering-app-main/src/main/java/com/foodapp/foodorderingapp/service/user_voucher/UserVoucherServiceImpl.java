package com.foodapp.foodorderingapp.service.user_voucher;

import com.foodapp.foodorderingapp.dto.user_voucher.UserVoucherRequest;
import com.foodapp.foodorderingapp.dto.user_voucher.UserVoucherResponse;
import com.foodapp.foodorderingapp.entity.*;
import com.foodapp.foodorderingapp.enumeration.VoucherStatus;
import com.foodapp.foodorderingapp.helper.UserHelper;
import com.foodapp.foodorderingapp.repository.ProductDiscountJpaRepository;
import com.foodapp.foodorderingapp.repository.VoucherApplicationJpaRepository;
import com.foodapp.foodorderingapp.repository.VoucherJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserVoucherServiceImpl implements UserVoucherService   {
    private final ProductDiscountJpaRepository productDiscountJpaRepository;
    private final VoucherJpaRepository voucherJpaRepository;
    private  final VoucherApplicationJpaRepository voucherApplicationJpaRepository;
    private final UserHelper userHelper;
    @Override
    public Voucher receiveVoucher(UserVoucherRequest createUserVoucherRequest) {
        //find product discount by code or product discount id
        ProductDiscount productDiscount;
        if(createUserVoucherRequest.getCode() != null){
            productDiscount = productDiscountJpaRepository.findByCouponCode(createUserVoucherRequest.getCode()).orElseThrow(() -> new RuntimeException("Product discount not found by code"));
        }
        else{
            productDiscount = productDiscountJpaRepository.findById(createUserVoucherRequest.getProductDiscountId()).orElseThrow(() -> new RuntimeException("Product discount not found"));
        }
        User user = userHelper.findCurrentUser().orElseThrow(() -> new RuntimeException("User not found")   );
        Optional<Voucher> voucher = voucherJpaRepository.findFirstByProductDiscountAndUser(productDiscount, user);
        if(voucher.isPresent()){
            Voucher existingVoucher = voucher.get();
            int remainingUsed = existingVoucher.getRemainingUsage();
            existingVoucher.setRemainingUsage(remainingUsed + 1);
            return voucherJpaRepository.save(existingVoucher);
        }
        else{
            Voucher newVoucher = new Voucher();
            newVoucher.setProductDiscount(productDiscount);
            newVoucher.setStatus(VoucherStatus.ACTIVE);
            //find current user
            newVoucher.setUser(user);
            newVoucher.setRemainingUsage(1);
            //save voucher
            return voucherJpaRepository.save(newVoucher);
        }
        //create new voucher
    }

    @Override
    @Transactional
    public Voucher useVoucher(Long voucherId, Order order) {
        //find active voucher with voucher id
        Voucher voucher = voucherJpaRepository.findById(voucherId).orElseThrow(() -> new RuntimeException("Voucher not found"));
        if(voucher.getStatus() != VoucherStatus.ACTIVE) throw new RuntimeException("Voucher is not active");
        //create new voucher application object
        VoucherApplication voucherApplication = new VoucherApplication();
        voucherApplication.setVoucher(voucher);
        voucherApplication.setOrder(order);
        //update voucher remaining usage
        if(voucher.getRemainingUsage() > 1){
            voucher.setRemainingUsage(voucher.getRemainingUsage() - 1);
        }
        else{
            voucher.setRemainingUsage(0);
            voucher.setStatus(VoucherStatus.USED);
        }
        voucherJpaRepository.save(voucher);
        return voucherApplicationJpaRepository.save(voucherApplication).getVoucher();
    }
    @Override
    public UserVoucherResponse tryApplyVoucher(Long voucherId) {
        //find voucher by voucher id
          Optional<Voucher> voucher =  voucherJpaRepository.findById(voucherId);
          if(voucher.isEmpty() || voucher.get().getStatus() != VoucherStatus.ACTIVE) return new UserVoucherResponse(null, false);
          else{
              //return product discount object
                ProductDiscount productDiscount = productDiscountJpaRepository.findById(voucher.get().getProductDiscount().getId()).get();
                return new UserVoucherResponse(productDiscount, true);
          }
    }

    @Override
    public List<Voucher> findVouchersByStatusAndUserId(VoucherStatus voucherStatus) {
        User currentUser = userHelper.findCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
        return voucherJpaRepository.findAllByStatusAndUserId(voucherStatus, currentUser.getId());
    }
}
