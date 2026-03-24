package web_prac.model;

import jakarta.validation.constraints.*;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
    name = "place",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_place",
            columnNames = {"room_num", "shelf_num"}
        )
    }
)
public class Place implements Common<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_place")
    private Integer id;

    @Column(name = "room_num", nullable = false)
    private Integer roomNum;

    @Column(name = "shelf_num", nullable = false)
    private Integer shelfNum;

    @Positive
    @Column(name = "kg_limit", nullable = false)
    private Double kgLimit;
}