package com.easylive.admin.controller;


import com.easylive.component.VideoPlayerComponent;
import com.easylive.config.AdminConfig;
import com.easylive.constants.Constants;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.DateTimePatternEnum;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.VideoInfoFilePostService;
import com.easylive.utils.DateUtils;
import com.easylive.utils.FFmpegUtils;
import com.easylive.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Date;

@RestController
@Validated
@RequestMapping("/file")
public class FileController extends ABaseController{
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    @Resource
    private AdminConfig adminConfig;

    @Resource
    private FFmpegUtils fFmpegUtils;
    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoPlayerComponent videoPlayerComponent;

    @RequestMapping("/uploadImage")
    public ResponseVO uploadFile(@NotNull MultipartFile file, @NotNull boolean isCreateThumbnail) throws IOException {
        //创建上传文件保存路径
        String mouth = DateUtils.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
        String folderPath = adminConfig.getProjectFolder()
                + Constants.FILE_PATH_FOLDER + Constants.FILE_PATH_FOLDER_COVER
                + mouth;

        //获取文件后缀".xxx"
        String originalFilename = file.getOriginalFilename();
        String fileSuffix = StringTools.getFileSuffix(originalFilename);
        //设置文件名
        String fileRealName = StringTools.generateRandomStr(Constants.LENGTH_30) + fileSuffix;
        File folder = new File(folderPath);

        if (!folder.exists())
        {
            folder.mkdirs();
        }

        //将上传文件转移到指定目录
        String filePath = folderPath + "/" +  fileRealName;
        file.transferTo(new File(filePath));
        if (isCreateThumbnail)
        {
            //生成缩略图
            fFmpegUtils.createImageThumbnail(filePath);
        }
        //返回图片路径
        return getSuccessResponseVO(Constants.FILE_PATH_FOLDER_COVER + mouth + "/" + fileRealName);
    }

    /**
     * @description 前端用uploadFile返回的地址向getResource发起get请求
     * @param response 返回图片
     * @param sourcePath 文件相对路径地址
     */
    @RequestMapping("/getResource")
    public void getResource(HttpServletResponse response,  @RequestParam("sourceName") @NotNull String sourcePath)
    {
        if (sourcePath == null || !StringTools.pathIsOk(sourcePath))
        {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String suffixName = StringTools.getFileSuffix(sourcePath);

        //设置图片类型
        response.setContentType("image/" + suffixName.replace(".", ""));
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, sourcePath);
    }

    @RequestMapping("/videoResource/{fileId}/")
    public ResponseEntity<UrlResource> videoResource(@PathVariable String fileId) throws MalformedURLException {
        return videoPlayerComponent.videoResource(fileId, adminConfig.getProjectFolder());
    }

    @RequestMapping("/videoResource/{fileId}/{name}")
    public ResponseEntity<UrlResource> videoResource(@PathVariable String fileId, @PathVariable String name) throws MalformedURLException {
        return videoPlayerComponent.videoResource(fileId, name, adminConfig.getProjectFolder());
    }


    protected void readFile(HttpServletResponse response, String filePath) {
        File file = new File(adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + filePath);
        if (!file.exists())
            return;

        try (OutputStream out = response.getOutputStream(); FileInputStream in = new FileInputStream(file)) {
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = in.read(bytes)) != -1)
            {
                out.write(bytes, 0, len);
            }
        } catch (Exception e)
        {
            log.error("读取文件异常", e);
        }
    }



}
