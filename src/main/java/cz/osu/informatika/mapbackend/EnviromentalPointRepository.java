package cz.osu.informatika.mapbackend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnviromentalPointRepository extends JpaRepository<EnviromentalPoint, Long> {
    // Tady máme automaticky metody jako save(), findAll(), findById()
}
