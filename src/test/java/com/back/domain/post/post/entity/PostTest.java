package com.back.domain.post.post.entity;

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
}
