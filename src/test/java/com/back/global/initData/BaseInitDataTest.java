package com.back.global.initData;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest         // 스프링을 띄운다 → BaseInitData 도 실행되어 샘플 글이 만들어진다
@ActiveProfiles("test")
@Transactional
public class BaseInitDataTest {
    @Autowired
    private PostService postService;

    @Test
    @DisplayName("앱이 시작되면 샘플 글 3건이 자동으로 만들어져 있다")
    void t1() {
        assertThat(postService.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("샘플 글의 제목은 제목 1, 제목 2, 제목 3 이고 id 는 1, 2, 3 이다")
    void t2() {
        List<Post> posts = postService.findAll();

        assertThat(posts)
                .extracting(Post::getTitle)
                .containsExactly("제목 1", "제목 2", "제목 3");
        assertThat(posts)
                .extracting(Post::getId)
                .containsExactly(1L, 2L, 3L);
    }
}
