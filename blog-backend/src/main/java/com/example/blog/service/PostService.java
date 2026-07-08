package com.example.blog.service;

import com.example.blog.constant.PostConstants;
import com.example.blog.dto.request.PostCreateRequest;
import com.example.blog.dto.response.PageResult;
import com.example.blog.entity.Post;
import com.example.blog.entity.Tag;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文章服务类
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Service
public class PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private TagService tagService;

    /**
     * 验证文章所有权
     */
    private void validatePostOwnership(Post post, Long authorId) {
        if (!post.getAuthorId().equals(authorId)) {
            throw new BusinessException("无权操作该文章");
        }
    }

    /**
     * 创建文章
     *
     * @param authorId 作者 ID
     * @param request  创建请求
     * @return 创建的文章
     */
    @Transactional
    @CacheEvict(value = {"posts"}, allEntries = true)
    public Post createPost(Long authorId, PostCreateRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setCategoryId(request.getCategoryId());
        post.setAuthorId(authorId);
        post.setStatus(PostConstants.STATUS_DRAFT);
        post.setFeaturedImage(request.getFeaturedImage());

        postMapper.insert(post);

        // 保存标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tagService.addTagsToPost(post.getId(), request.getTagIds());
        }

        return post;
    }

    /**
     * 更新文章
     *
     * @param postId   文章 ID
     * @param authorId 作者 ID
     * @param request  更新请求
     * @return 更新后的文章
     */
    @Transactional
    @CacheEvict(value = {"posts"}, allEntries = true)
    public Post updatePost(Long postId, Long authorId, PostCreateRequest request) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }

        validatePostOwnership(post, authorId);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setCategoryId(request.getCategoryId());
        post.setFeaturedImage(request.getFeaturedImage());

        postMapper.updateById(post);

        // 更新标签关联
        if (request.getTagIds() != null) {
            tagService.addTagsToPost(post.getId(), request.getTagIds());
        }

        return post;
    }

    /**
     * 发布文章
     *
     * @param postId   文章 ID
     * @param authorId 作者 ID
     * @return 发布后的文章
     */
    @CacheEvict(value = {"posts", "hotPosts", "archiveStats"}, allEntries = true)
    public Post publishPost(Long postId, Long authorId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }

        validatePostOwnership(post, authorId);

        post.setStatus(PostConstants.STATUS_PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        postMapper.updateById(post);
        return post;
    }

    /**
     * 取消发布文章
     *
     * @param postId   文章 ID
     * @param authorId 作者 ID
     * @return 取消发布后的文章
     */
    @CacheEvict(value = {"posts", "hotPosts", "archiveStats"}, allEntries = true)
    public Post unpublishPost(Long postId, Long authorId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }

        validatePostOwnership(post, authorId);

        post.setStatus(PostConstants.STATUS_DRAFT);
        post.setPublishedAt(null);
        postMapper.updateById(post);
        return post;
    }

    /**
     * 删除文章（移至回收站）
     *
     * @param postId   文章 ID
     * @param authorId 作者 ID
     */
    @CacheEvict(value = {"posts", "hotPosts", "archiveStats"}, allEntries = true)
    public void deletePost(Long postId, Long authorId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }

        validatePostOwnership(post, authorId);

        post.setStatus(PostConstants.STATUS_DELETED);
        postMapper.updateById(post);
    }

    /**
     * 获取回收站中的文章
     *
     * @return 回收站文章列表
     */
    public List<Post> getDeletedPosts() {
        return postMapper.findDeletedPosts();
    }

    /**
     * 获取指定作者的回收站文章
     *
     * @param authorId 作者 ID
     * @return 回收站文章列表
     */
    public List<Post> getDeletedPostsByAuthor(Long authorId) {
        return postMapper.findDeletedPostsByAuthor(authorId);
    }

    /**
     * 恢复已删除的文章
     *
     * @param postId   文章 ID
     * @param authorId 作者 ID
     * @return 恢复后的文章
     */
    @Transactional
    @CacheEvict(value = {"posts"}, allEntries = true)
    public Post restorePost(Long postId, Long authorId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }

        validatePostOwnership(post, authorId);

        if (!PostConstants.STATUS_DELETED.equals(post.getStatus())) {
            throw new BusinessException("该文章不在回收站中");
        }

        // 恢复到草稿状态
        post.setStatus(PostConstants.STATUS_DRAFT);
        post.setPublishedAt(null);
        postMapper.updateById(post);
        return post;
    }

    /**
     * 永久删除文章
     *
     * @param postId   文章 ID
     * @param authorId 作者 ID
     */
    @Transactional
    @CacheEvict(value = {"posts", "hotPosts", "archiveStats"}, allEntries = true)
    public void permanentlyDeletePost(Long postId, Long authorId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }

        validatePostOwnership(post, authorId);

        if (!PostConstants.STATUS_DELETED.equals(post.getStatus())) {
            throw new BusinessException("只能永久删除回收站中的文章");
        }

        // 删除标签关联
        tagService.removeTagsFromPost(postId);

        // 永久删除文章
        postMapper.deleteById(postId);
    }

    /**
     * 获取已发布文章列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @Cacheable(value = {"posts"}, key = "'published_' + #page + '_' + #size")
    public PageResult<Post> getPublishedPosts(int page, int size) {
        int offset = (page - 1) * size;
        List<Post> posts = postMapper.findPublishedPosts(offset, size);
        int total = postMapper.countPublishedPosts();

        return PageResult.of(posts, total, page, size);
    }

    /**
     * 查看文章（增加浏览次数）
     *
     * @param postId 文章 ID
     * @return 文章详情
     */
    public Post viewPost(Long postId) {
        Post post = postMapper.findPublishedPostById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }

        postMapper.incrementViewCount(postId);
        post.setViewCount(post.getViewCount() + 1);

        // 加载文章的标签
        List<Tag> tags = tagService.getTagsByPostId(postId);
        post.setTags(tags);

        return post;
    }

    /**
     * 获取作者的文章列表
     *
     * @param authorId 作者 ID
     * @return 文章列表
     */
    public List<Post> getPostsByAuthor(Long authorId) {
        return postMapper.findByAuthorId(authorId);
    }

    /**
     * 获取所有文章（管理员用，不含已删除）
     *
     * @return 所有文章列表
     */
    public List<Post> getAllPosts() {
        return postMapper.findAllNonDeletedPosts();
    }

    /**
     * 搜索文章
     *
     * @param keyword    关键词
     * @param categoryId 分类 ID
     * @param page       页码
     * @param size       每页大小
     * @return 分页结果
     */
    public PageResult<Post> searchPosts(String keyword, Long categoryId, int page, int size) {
        int offset = (page - 1) * size;
        List<Post> posts = postMapper.searchPosts(keyword, categoryId, offset, size);
        int total = postMapper.countSearchPosts(keyword, categoryId);
        return PageResult.of(posts, total, page, size);
    }

    /**
     * 获取热门文章
     *
     * @param limit 数量限制
     * @return 热门文章列表
     */
    @Cacheable(value = {"hotPosts"}, key = "'limit_' + #limit")
    public List<Post> getHotPosts(int limit) {
        return postMapper.findHotPosts(limit);
    }

    /**
     * 获取归档统计
     *
     * @return 归档统计列表
     */
    @Cacheable(value = {"archiveStats"}, key = "'all'")
    public List<Map<String, Object>> getArchiveStats() {
        return postMapper.getArchiveStats();
    }

    /**
     * 获取标签下的文章列表
     *
     * @param tagId 标签 ID
     * @param page  页码
     * @param size  每页大小
     * @return 分页结果
     */
    public PageResult<Post> getPostsByTag(Long tagId, int page, int size) {
        int offset = (page - 1) * size;
        List<Post> posts = postMapper.findPostsByTag(tagId, offset, size);
        int total = postMapper.countPostsByTag(tagId);
        return PageResult.of(posts, total, page, size);
    }
}
