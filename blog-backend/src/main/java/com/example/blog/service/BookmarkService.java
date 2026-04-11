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
public class BookmarkService {

    @Autowired
    private BookmarkMapper bookmarkMapper;

    @Autowired
    private PostMapper postMapper;

    @Transactional
    public void bookmarkPost(Long postId, Long userId) {
        // Check if post exists
        if (postMapper.selectById(postId) == null) {
            throw new BusinessException("文章不存在");
        }

        // Check if already bookmarked
        Bookmark existingBookmark = bookmarkMapper.findByUserIdAndPostId(userId, postId);
        if (existingBookmark != null) {
            throw new BusinessException("文章已收藏");
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setPostId(postId);
        bookmark.setUserId(userId);

        bookmarkMapper.insert(bookmark);
    }

    @Transactional
    public void removeBookmark(Long postId, Long userId) {
        Bookmark existingBookmark = bookmarkMapper.findByUserIdAndPostId(userId, postId);
        if (existingBookmark == null) {
            throw new BusinessException("收藏记录不存在");
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

    public int countUserBookmarks(Long userId) {
        return bookmarkMapper.countByUserId(userId);
    }
}
