package com.foodapp.foodorderingapp.entity;

import com.foodapp.foodorderingapp.enumeration.VoucherStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vouchers")
@Entity
@Getter
@Builder
public class Voucher {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private ProductDiscount productDiscount;
    @Enumerated(EnumType.STRING)
    private VoucherStatus status;
    private int remainingUsage;
    @OneToMany(mappedBy = "voucher")
    private List<VoucherApplication> voucherApplications;
}
