package com.leyou.common.advice;

import com.leyou.common.exceptions.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class BasicExceptionAdvice {

    @ExceptionHandler(LyException.class)
    public ResponseEntity<String> handleLyException(LyException e) {
        log.error(e.getMessage());
        // 我们暂定返回状态码为400， 然后从异常中获取友好提示信息
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }
}
