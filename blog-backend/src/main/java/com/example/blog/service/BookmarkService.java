package com.example.blog.service;

import com.example.blog.entity.Bookmark;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.BookmarkMapper;
import com.example.blog.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookmarkService {

    @Autowired
    private BookmarkMapper bookmarkMapper;

    @Autowired
    private PostMapper postMapper;

    public void bookmarkPost(Long postId, Long userId) {
        // Check if post exists
        if (postMapper.selectById(postId) == null) {
            throw new BusinessException("Post not found");
        }

        // Check if already bookmarked
        Bookmark existingBookmark = bookmarkMapper.findByUserIdAndPostId(userId, postId);
        if (existingBookmark != null) {
            throw new BusinessException("Post already bookmarked");
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setPostId(postId);
        bookmark.setUserId(userId);

        bookmarkMapper.insert(bookmark);
    }

    public void removeBookmark(Long postId, Long userId) {
        Bookmark existingBookmark = bookmarkMapper.findByUserIdAndPostId(userId, postId);
        if (existingBookmark == null) {
            throw new BusinessException("Bookmark not found");
        }

        bookmarkMapper.deleteBookmark(userId, postId);
    }

    public boolean isPostBookmarkedByUser(Long postId, Long userId) {
        return bookmarkMapper.findByUserIdAndPostId(userId, postId) != null;
    }

    public List<Bookmark> getUserBookmarks(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return bookmarkMapper.findBookmarksByUserId(userId, offset, size);
    }
}