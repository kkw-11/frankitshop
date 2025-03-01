package com.shop.frankit.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_options")
@Getter
@NoArgsConstructor
public class ProductOption extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private OptionType type;

    @Column(nullable = false)
    @Setter
    private BigDecimal additionalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @Setter
    private Product product;

    @OneToMany(mappedBy = "productOption", cascade = CascadeType.ALL)
    @Setter
    private List<OptionValue> optionValues = new ArrayList<>();

    public enum OptionType {
        INPUT, // 사용자 입력 타입
        SELECT // 선택 타입
    }
}
