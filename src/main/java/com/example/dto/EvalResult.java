package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author liushug
 * @date 2026/9/15 10:45
 * @description 返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvalResult {

    private String question;

    private String groundTruth;

    private String answer;

    private List<String> context;

}
