package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.entity.Music;
import com.example.blog.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/music")
@CrossOrigin
public class MusicController {

    @Autowired
    private MusicService musicService;

    /**
     * 获取所有音乐
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Music>>> getAllMusic() {
        List<Music> musicList = musicService.getAllMusic();
        return ResponseEntity.ok(ApiResponse.success(musicList));
    }

    /**
     * 获取单个音乐
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Music>> getMusicById(@PathVariable Long id) {
        Music music = musicService.getMusicById(id);
        if (music == null) {
            return ResponseEntity.ok(ApiResponse.error("Music not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(music));
    }

    /**
     * 创建音乐（Admin）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Music>> createMusic(@RequestBody Music music) {
        Music created = musicService.createMusic(music);
        return ResponseEntity.ok(ApiResponse.success("Music created successfully", created));
    }

    /**
     * 更新音乐（Admin）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Music>> updateMusic(@PathVariable Long id, @RequestBody Music music) {
        Music updated = musicService.updateMusic(id, music);
        if (updated == null) {
            return ResponseEntity.ok(ApiResponse.error("Music not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Music updated successfully", updated));
    }

    /**
     * 删除音乐（Admin）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMusic(@PathVariable Long id) {
        boolean deleted = musicService.deleteMusic(id);
        if (!deleted) {
            return ResponseEntity.ok(ApiResponse.error("Music not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Music deleted successfully", null));
    }
}
