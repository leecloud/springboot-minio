package com.example.springbootminio.controller;

import com.example.springbootminio.config.CommonConstants;
import com.google.api.client.util.IOUtils;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MinioController
 * @Description:
 * @Author jiang
 * @Date 2020/9/17 11:34
 **/
@RestController
@RequestMapping("/minio")
public class MinioController {
    @Autowired
    private MinioClient minioClient;

    @ResponseBody
    @PostMapping("/upload")
    public String upload(@RequestParam(name = "file",required = false) MultipartFile file, HttpServletRequest request) throws Exception {
        if(file==null || file.getSize()==0){
            throw  new Exception("上传文件不能为空");
        }
        String orgfileName = file.getOriginalFilename();
        try {
            InputStream in = file.getInputStream();
            String contentType= file.getContentType();
            minioClient.putObject(CommonConstants.MINIO_BUCKET,orgfileName,in,null, null, null, contentType);
            Map<String,Object> data=new HashMap<>();
            data.put("bucketName",CommonConstants.MINIO_BUCKET);
            data.put("fileName",orgfileName);
            return data.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new Exception("上传失败");
    }

    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response, String fileName) {
        InputStream in=null;
        try {
            //获取文件对象 stat原信息
            ObjectStat stat =minioClient.statObject(CommonConstants.MINIO_BUCKET, fileName);
            response.setContentType(stat.contentType());
            in =   minioClient.getObject(CommonConstants.MINIO_BUCKET, fileName);
            IOUtils.copy(in,response.getOutputStream());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
