package com.example.dto;

import com.example.config.SseState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liushug
 * @date 2026/9/14 22:06
 * @description SSE 事件
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SseEvent {

    private SseState state;

    private String think;

    private String message;



    public static SseEvent state(SseState state){
        return new SseEvent(state, null, null);
    }

    public static SseEvent think(String think){
        return new SseEvent(null, think, null);
    }

    public static SseEvent message(String message){
        return new SseEvent(null, null, message);
    }

}
