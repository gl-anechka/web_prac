package web_prac.model;

import jakarta.validation.constraints.*;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "reception")
public class Reception implements Common<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reception")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_consumer", nullable = false)
    private Partner consumer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    @Positive
    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;
}