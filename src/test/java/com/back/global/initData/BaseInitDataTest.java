package com.back.global.initData;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
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

    // ---------- [1-6 추가] ----------

    @Test
    @DisplayName("앱이 시작되면 샘플 댓글 5건이 DB에 저장되어 있다")
    void t3() {
        // findAll() 로 DB에서 글을 새로 읽어 오고, 각 글의 댓글 수를 더한다
        int totalComments = postService.findAll().stream()
                .mapToInt(post -> post.getComments().size())
                .sum();

        assertThat(totalComments).isEqualTo(5);
    }

    @Test
    @DisplayName("1번 글에 댓글 3건, 2번 글에 댓글 2건, 3번 글에는 댓글이 없다")
    void t4() {
        List<Post> posts = postService.findAll();

        assertThat(posts.get(0).getComments())
                .extracting(PostComment::getContent)
                .containsExactly("댓글 1-1", "댓글 1-2", "댓글 1-3");
        assertThat(posts.get(1).getComments())
                .extracting(PostComment::getContent)
                .containsExactly("댓글 2-1", "댓글 2-2");
        assertThat(posts.get(2).getComments()).isEmpty();
    }
}
