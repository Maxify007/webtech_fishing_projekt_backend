package com.example.fishing;

import com.example.fishing.persistence.entity.Fisher;
import com.example.fishing.persistence.entity.FisherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FisherRepositoryTest {

    @Autowired
    private FisherRepository fisherRepository;

    @Test
    @DisplayName("Leaderboard Tests gives 10 Fishers Sorted")
    void findTop10ByOrderByTotalFishAmountDesc_returnsTop10Sorted() {
        // given: create more than 10 fishers with varying totalFishAmount
        for (int i = 0; i < 20; i++) {
            Fisher f = new Fisher(1L, "Fisher-" + i);
            f.setTotalFishAmount(i * 100L); // increasing
            fisherRepository.save(f);
        }

        // when
        List<Fisher> top = fisherRepository.findTop10ByOrderByTotalFishAmountDesc();

        // then
        assertThat(top).hasSize(10);
        // first item should have highest totalFishAmount
        assertThat(top.get(0).getTotalFishAmount())
                .isGreaterThanOrEqualTo(top.get(1).getTotalFishAmount());

        // ensure sorted descending
        for (int i = 0; i < top.size() - 1; i++) {
            long current = top.get(i).getTotalFishAmount();
            long next = top.get(i + 1).getTotalFishAmount();
            assertThat(current).isGreaterThanOrEqualTo(next);
        }
    }
}

