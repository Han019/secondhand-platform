package com.secondhand.platform.productimage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "pending_image_deletions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PendingImageDeletion {
    @Id
    @Column(length = 1024)
    private String imagePath;

    public PendingImageDeletion(String imagePath) {
        this.imagePath = imagePath;
    }
}
