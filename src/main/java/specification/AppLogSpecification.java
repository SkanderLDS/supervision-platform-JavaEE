package specification;
import com.vermeg.platform.supervision_platform.Entity.AppLog;
import com.vermeg.platform.supervision_platform.Entity.LogLevel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppLogSpecification {

    public static Specification<AppLog> filter(
            Long serverId,
            LogLevel level,
            LocalDateTime from,
            LocalDateTime to,
            String keyword) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by server
            predicates.add(cb.equal(root.get("server").get("id"), serverId));

            // Filter by level if provided
            if (level != null) {
                predicates.add(cb.equal(root.get("level"), level));
            }

            // Filter by date range if provided
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }

            // Filter by keyword if provided
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("message")),
                        "%" + keyword.toLowerCase() + "%"
                ));
            }

            query.orderBy(cb.desc(root.get("timestamp")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}