package com.example.blog.service;

import com.example.blog.entity.PostTag;
import com.example.blog.entity.Tag;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.PostTagMapper;
import com.example.blog.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostTagMapper postTagMapper;

    /**
     * 创建标签
     */
    public Tag createTag(String name, String slug, String description) {
        // 检查标签名是否已存在
        Tag existing = tagMapper.selectByName(name);
        if (existing != null) {
            throw new BusinessException("Tag name already exists");
        }

        // 检查 slug 是否已存在
        existing = tagMapper.selectBySlug(slug);
        if (existing != null) {
            throw new BusinessException("Tag slug already exists");
        }

        Tag tag = new Tag();
        tag.setName(name);
        tag.setSlug(slug);
        tag.setDescription(description);

        tagMapper.insert(tag);
        return tag;
    }

    /**
     * 更新标签
     */
    public Tag updateTag(Long id, String name, String slug, String description) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("Tag not found");
        }

        // 检查新名称是否已被其他标签使用
        if (!tag.getName().equals(name)) {
            Tag existing = tagMapper.selectByName(name);
            if (existing != null && !existing.getId().equals(id)) {
                throw new BusinessException("Tag name already exists");
            }
        }

        // 检查新 slug 是否已被其他标签使用
        if (!tag.getSlug().equals(slug)) {
            Tag existing = tagMapper.selectBySlug(slug);
            if (existing != null && !existing.getId().equals(id)) {
                throw new BusinessException("Tag slug already exists");
            }
        }

        tag.setName(name);
        tag.setSlug(slug);
        tag.setDescription(description);

        tagMapper.updateById(tag);
        return tag;
    }

    /**
     * 删除标签
     */
    public void deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("Tag not found");
        }

        // 检查标签是否被使用
        int count = postTagMapper.countByTagId(id);
        if (count > 0) {
            throw new BusinessException("Cannot delete tag that is used by posts");
        }

        tagMapper.deleteById(id);
    }

    /**
     * 获取所有标签
     */
    public List<Tag> getAllTags() {
        return tagMapper.findAllWithPostCount();
    }

    /**
     * 获取标签详情
     */
    public Tag getTagById(Long id) {
        Tag tag = tagMapper.selectWithPostCount(id);
        if (tag == null) {
            throw new BusinessException("Tag not found");
        }
        return tag;
    }

    /**
     * 按 slug 获取标签
     */
    public Tag getTagBySlug(String slug) {
        Tag tag = tagMapper.selectBySlug(slug);
        if (tag == null) {
            throw new BusinessException("Tag not found");
        }
        return tag;
    }

    /**
     * 搜索标签
     */
    public List<Tag> searchTags(String keyword, int offset, int limit) {
        return tagMapper.searchTags(keyword, offset, limit);
    }

    /**
     * 为文章添加标签
     */
    public void addTagsToPost(Long postId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // 先删除原有的标签关联
        postTagMapper.deleteByPostId(postId);

        // 批量插入新的标签关联
        List<PostTag> postTags = tagIds.stream()
                .map(tagId -> {
                    PostTag postTag = new PostTag();
                    postTag.setPostId(postId);
                    postTag.setTagId(tagId);
                    return postTag;
                })
                .collect(Collectors.toList());

        postTagMapper.batchInsert(postTags);
    }

    /**
     * 获取文章的标签列表
     */
    public List<Tag> getTagsByPostId(Long postId) {
        return tagMapper.findByPostId(postId);
    }

    /**
     * 获取热门标签（按使用次数）
     */
    public List<Tag> getHotTags(int limit) {
        List<Tag> tags = tagMapper.findAllWithPostCount();
        return tags.stream()
                .filter(tag -> tag.getPostCount() != null && tag.getPostCount() > 0)
                .sorted((a, b) -> b.getPostCount() - a.getPostCount())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
