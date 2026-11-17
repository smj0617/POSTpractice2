package com.back.domain.post.post.service;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.postComment.entity.PostComment; // [4-1 추가]
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // [4-1 추가]

import java.util.List;
import java.util.Optional;

// 비즈니스 로직 담당 ("글을 쓴다", "글이 몇 개인지 센다" 같은 기능 단위)
// 컨트롤러(2단계에서 추가)는 서비스를 부르고, 서비스는 리포지터리를 부른다
@Service // 스프링이 이 클래스의 객체를 1개 만들어 관리한다(빈 등록)
@RequiredArgsConstructor // final 필드를 받는 생성자를 자동 생성 → 스프링이 PostRepository 를 넣어 준다(의존성 주입)
public class PostService {
    private final PostRepository postRepository;

    // 전체 글 개수
    public long count() {
        return postRepository.count(); // SELECT count(*) FROM post
    }

    // 글 작성: 객체를 만들고 → DB에 저장
    public Post write(String title, String content) {
        Post post = new Post(title, content); // 이 시점에는 id 가 null
        return postRepository.save(post);     // INSERT 실행 → id 가 채워진 Post 가 돌아온다
    }

    // [1-4 추가] 전체 글 목록
    public List<Post> findAll() {
        return postRepository.findAll(); // SELECT * FROM post
    }

    // [2-2 추가] id 로 글 1개 찾기. 없을 수도 있으므로 Optional 로 감싸서 돌려준다
    public Optional<Post> findById(long id) {
        return postRepository.findById(id); // SELECT * FROM post WHERE id = ?
    }

    // [3-1 추가] 글 삭제
    public void delete(Post post) {
        postRepository.delete(post); // DELETE FROM post WHERE id = ?
    }

    // [4-1 추가] 댓글 작성
    // 댓글은 글을 통해서 만든다(post.addComment) → 1-6 샘플 댓글과 같은 방식
    // @Transactional : addComment() 는 save() 를 부르지 않는 "객체 수정"이라서
    //                  트랜잭션 안에서 실행돼야 cascade PERSIST 로 저장된다 (1-6 참고)
    @Transactional
    public PostComment writeComment(Post post, String content) {
        PostComment postComment = post.addComment(content); // 이 시점에는 댓글 id 가 null

        // flush : 모아 둔 SQL 을 지금 바로 DB로 보낸다 → INSERT 실행 → 댓글 id 가 채워진다
        // (안 하면 메서드가 끝날 때 INSERT 되므로, 응답에 id 가 null 로 나간다)
        postRepository.flush();

        return postComment;
    }

    // [5-1 추가] 댓글 삭제
    // post.deleteComment() 도 save()/delete() 를 부르지 않는 "객체 수정"이라서 @Transactional 이 필요하다
    // 트랜잭션이 끝날 때 JPA 가 "목록에서 빠진 댓글"을 알아채고 DELETE 한다 (orphanRemoval)
    @Transactional
    public void deleteComment(Post post, PostComment postComment) {
        post.deleteComment(postComment);
    }
}
