package com.back.domain.post.post.entity;

import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // [5-1 추가]

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE; // [3-2 추가]
import static lombok.AccessLevel.PROTECTED;

// @Entity 가 붙으면 이 클래스를 보고 DB에 post 테이블이 만들어진다
// 칼럼: id, create_date, modify_date (BaseEntity 에서 상속) + title, content
@Entity
@Getter // getTitle(), getContent() 등을 자동 생성
@NoArgsConstructor(access = PROTECTED) // JPA가 DB에서 꺼낼 때 쓰는 기본 생성자. 밖에서 new Post() 는 못 하게 막음
public class Post extends BaseEntity {
    private String title;

    @Column(columnDefinition = "TEXT") // 긴 글도 들어가도록 TEXT 타입
    private String content;

    // [1-5 추가] 글 1개 : 댓글 N개 (One 글 To Many 댓글)
    // mappedBy = "post" : 외래키(post_id)는 내가 아니라 PostComment 의 post 필드가 관리한다는 뜻
    //                     → post 테이블에는 칼럼이 생기지 않는다
    // [1-6 추가] cascade = PERSIST : 글을 저장(영속화)할 때, comments 에 새로 추가된 댓글도 같이 저장한다
    //                               → 댓글마다 save() 를 부르지 않아도 INSERT 된다
    // [3-2 수정] cascade 에 REMOVE 추가 : 글을 삭제할 때, 달린 댓글들도 먼저 같이 삭제한다
    //                                    → 댓글이 남아 있어서 생기는 외래키 에러가 사라진다
    // [5-1 추가] orphanRemoval = true : comments 목록에서 빠진 댓글(고아)은 DB에서도 삭제한다
    //                                  → comments.remove(댓글) 만 하면 DELETE 가 실행된다
    @OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    // 글을 만들 때는 이 생성자를 쓴다 (id, 날짜는 저장할 때 자동으로 채워짐)
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // [1-5 추가] 댓글은 반드시 글을 통해서 만든다
    // 양쪽(글의 comments 목록, 댓글의 post 필드)을 한 번에 연결해서 빠뜨릴 일이 없게 한다
    public PostComment addComment(String content) {
        PostComment postComment = new PostComment(this, content); // 댓글 → 글 연결
        comments.add(postComment);                                 // 글 → 댓글 연결
        return postComment;
    }

    // [5-1 추가] 이 글의 댓글 중에서 id 로 1개 찾기 (없으면 빈 Optional)
    // 이 글의 댓글 목록에서만 찾으므로, 다른 글의 댓글 id 를 넣으면 못 찾는다
    public Optional<PostComment> findCommentById(long id) {
        return comments.stream()
                .filter(comment -> comment.getId() == id)
                .findFirst();
    }

    // [5-1 추가] 댓글 삭제: 목록에서 빼기만 한다
    // orphanRemoval 덕분에 트랜잭션이 끝날 때 DELETE FROM post_comment WHERE id = ? 가 실행된다
    public void deleteComment(PostComment postComment) {
        comments.remove(postComment);
    }
}
