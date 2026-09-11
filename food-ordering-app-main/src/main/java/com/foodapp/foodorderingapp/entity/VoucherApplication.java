package com.foodapp.foodorderingapp.entity;

import com.foodapp.foodorderingapp.enumeration.VoucherStatus;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "voucher_applications")
@Entity
@Getter
@Builder
public class VoucherApplication {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Order order;
    @ManyToOne
    private Voucher voucher;
}

