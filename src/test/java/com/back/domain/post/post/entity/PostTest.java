package com.back.domain.post.post.entity;

import com.back.domain.post.postComment.entity.PostComment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 스프링을 띄우지 않는 순수 자바 테스트 → 아주 빠르다
public class PostTest {
    @Test
    @DisplayName("new Post(제목, 내용) 으로 만들면 제목과 내용이 들어간다")
    void t1() {
        Post post = new Post("제목", "내용");

        assertThat(post.getTitle()).isEqualTo("제목");
        assertThat(post.getContent()).isEqualTo("내용");
    }

    @Test
    @DisplayName("DB에 저장하기 전에는 id 와 생성일이 비어 있다(null)")
    void t2() {
        Post post = new Post("제목", "내용");

        // id 는 DB에 저장(INSERT)될 때 DB가 매겨 준다 → 아직 저장 전이라 null
        assertThat(post.getId()).isNull();
        assertThat(post.getCreateDate()).isNull();
    }

    // ---------- [1-5 추가] ----------

    @Test
    @DisplayName("새 글에는 댓글이 0개다")
    void t3() {
        Post post = new Post("제목", "내용");

        assertThat(post.getComments()).isEmpty();
    }

    @Test
    @DisplayName("addComment() 를 하면 글의 댓글 목록에 추가되고, 댓글도 자기 글을 안다 (양방향 연결)")
    void t4() {
        Post post = new Post("제목", "내용");

        PostComment comment1 = post.addComment("댓글 1");
        PostComment comment2 = post.addComment("댓글 2");

        // 글 → 댓글 방향
        assertThat(post.getComments()).containsExactly(comment1, comment2);
        // 댓글 → 글 방향
        assertThat(comment1.getPost()).isSameAs(post);
        assertThat(comment1.getContent()).isEqualTo("댓글 1");
    }
}
