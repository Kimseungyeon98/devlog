package com.devlog.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 500, message = "됓글은 500자 이하여야 합니다.")
        String content,

        Long parentId  // null이면 최상위 됓글, 값이 있으면 대됓글
) {}