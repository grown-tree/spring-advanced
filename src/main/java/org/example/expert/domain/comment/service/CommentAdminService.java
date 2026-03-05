package org.example.expert.domain.comment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentAdminService {

    private final CommentRepository commentRepository;

    @Transactional
    public void deleteComment(long commentId) {

        //댓글이없는 경우 예외 처리
        commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        commentRepository.deleteById(commentId);
    }
}
