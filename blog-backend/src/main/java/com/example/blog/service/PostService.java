package com.example.blog.service;

import com.example.blog.dto.request.PostCreateRequest;
import com.example.blog.dto.response.PageResult;
import com.example.blog.entity.Post;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PostService {

    @Autowired
    private PostMapper postMapper;

    public Post createPost(Long authorId, PostCreateRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setCategoryId(request.getCategoryId());
        post.setAuthorId(authorId);
        post.setStatus("DRAFT");

        postMapper.insert(post);
        return post;
    }

    public Post updatePost(Long postId, Long authorId, PostCreateRequest request) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }

        if (!post.getAuthorId().equals(authorId)) {
            throw new BusinessException("Unauthorized to update this post");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setCategoryId(request.getCategoryId());

        postMapper.updateById(post);
        return post;
    }

    public Post publishPost(Long postId, Long authorId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }

        if (!post.getAuthorId().equals(authorId)) {
            throw new BusinessException("Unauthorized to publish this post");
        }

        post.setStatus("PUBLISHED");
        post.setPublishedAt(LocalDateTime.now());
        postMapper.updateById(post);
        return post;
    }

    public Post unpublishPost(Long postId, Long authorId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }

        if (!post.getAuthorId().equals(authorId)) {
            throw new BusinessException("Unauthorized to unpublish this post");
        }

        post.setStatus("DRAFT");
        post.setPublishedAt(null);
        postMapper.updateById(post);
        return post;
    }

    public void deletePost(Long postId, Long authorId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }

        if (!post.getAuthorId().equals(authorId)) {
            throw new BusinessException("Unauthorized to delete this post");
        }

        post.setStatus("DELETED");
        postMapper.updateById(post);
    }

    public PageResult<Post> getPublishedPosts(int page, int size) {
        int offset = (page - 1) * size;
        List<Post> posts = postMapper.findPublishedPosts(offset, size);
        int total = postMapper.countPublishedPosts();

        return PageResult.of(posts, total, page, size);
    }

    public Post viewPost(Long postId) {
        Post post = postMapper.findPublishedPostById(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }

        postMapper.incrementViewCount(postId);
        post.setViewCount(post.getViewCount() + 1);

        return post;
    }

    public List<Post> getPostsByAuthor(Long authorId) {
        return postMapper.findByAuthorId(authorId);
    }
}