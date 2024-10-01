package com.sparklenote.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long stickerId;

    private String stickerName;

    @ManyToOne
    @JoinColumn(name = "paper_id")
    private Paper paper;
}
