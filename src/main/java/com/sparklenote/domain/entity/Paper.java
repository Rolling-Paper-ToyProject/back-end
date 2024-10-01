package com.sparklenote.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paper extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int paperId;

    private String content;

    private String sticker;

    @ManyToOne
    @JoinColumn(name = "roll_id")
    private Roll roll;

    @OneToMany(mappedBy = "paper")
    private List<Sticker> stickers = new ArrayList<>();

}
