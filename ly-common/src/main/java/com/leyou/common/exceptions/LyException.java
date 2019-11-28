package com.leyou.common.exceptions;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

@Getter
public class LyException extends RuntimeException {

    private int status;

    public LyException(ExceptionEnum en){
        super(en.getMessage());
        this.status = en.getStatus();
    }
    public LyException(ExceptionEnum en,Throwable cause){
        super(en.getMessage(),cause);
        this.status = en.getStatus();
    }
}
