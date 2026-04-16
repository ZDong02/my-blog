package com.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.entity.Music;
import com.example.blog.mapper.MusicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MusicService {

    @Autowired
    private MusicMapper musicMapper;

    /**
     * 获取所有音乐，按 sort_order 升序排列
     */
    public List<Music> getAllMusic() {
        return musicMapper.selectList(new LambdaQueryWrapper<Music>()
                .orderByAsc(Music::getSortOrder)
                .orderByDesc(Music::getId));
    }

    /**
     * 根据 ID 获取音乐
     */
    public Music getMusicById(Long id) {
        return musicMapper.selectById(id);
    }

    /**
     * 创建音乐
     */
    public Music createMusic(Music music) {
        musicMapper.insert(music);
        return music;
    }

    /**
     * 更新音乐
     */
    public Music updateMusic(Long id, Music music) {
        Music existing = musicMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        existing.setTitle(music.getTitle());
        existing.setArtist(music.getArtist());
        existing.setYoutubeId(music.getYoutubeId());
        existing.setCategory(music.getCategory());
        existing.setSortOrder(music.getSortOrder());
        existing.setCoverImage(music.getCoverImage());
        musicMapper.updateById(existing);
        return existing;
    }

    /**
     * 删除音乐
     */
    public boolean deleteMusic(Long id) {
        return musicMapper.deleteById(id) > 0;
    }
}
