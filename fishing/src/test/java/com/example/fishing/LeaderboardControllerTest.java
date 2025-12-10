package com.example.fishing;



import com.example.fishing.persistence.entity.Fisher;
import com.example.fishing.persistence.entity.FisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FisherRepository fisherRepository;

    @BeforeEach
    void setup() {
        fisherRepository.deleteAll();

        // create some fishers with different totalFishAmount
        IntStream.range(0, 5).forEach(i -> {
            Fisher f = new Fisher(1L, "Fisher-" + i);
            f.setTotalFishAmount((long) (i * 100));
            fisherRepository.save(f);
        });
    }

    @Test
    @DisplayName("GET /api/leaderboard returns JSON list of top fishers")
    void leaderboard_returnsTopFishers() throws Exception {
        var result = mockMvc.perform(get("/api/leaderboard")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        // Simple sanity checks: contains some of our fisher names
        assertThat(json).contains("Fisher-0");
        assertThat(json).contains("Fisher-1");
    }
}
