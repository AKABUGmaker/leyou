package com.leyou.upload.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {

    // 支持的文件类型
    private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg", "image/bmp");

    public String uploadImage(MultipartFile file) {

        try {
            //1,校验文件类型
            if (!suffixes.contains(file.getContentType())){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            //2,校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (null==image){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            //3，io保存

            String fileName = file.getOriginalFilename();
            file.transferTo(new File("D:/heima/heimaLeYou/"+fileName));

            //4,返回引用
            return "http://image.leyou.com/"+fileName;
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }
}
