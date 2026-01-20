package com.vermeg.platform.supervision_platform.Repository;

import com.vermeg.platform.supervision_platform.Entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
}
