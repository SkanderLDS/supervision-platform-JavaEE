package com.vermeg.platform.supervision_platform.Repository;
import com.vermeg.platform.supervision_platform.Entity.Application;;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository <Application, Long> {

}
