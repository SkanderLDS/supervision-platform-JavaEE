package com.vermeg.platform.supervision_platform.Repository;

import com.vermeg.platform.supervision_platform.Entity.ApplicationVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ApplicationVersionRepository  extends JpaRepository<ApplicationVersion, Long> {
    List<ApplicationVersion> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
