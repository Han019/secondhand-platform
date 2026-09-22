package com.secondhand.platform.productimage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingImageDeletionRepository extends JpaRepository<PendingImageDeletion, String> {
}
