package cn.gransier.oss.controller;

import cn.gransier.oss.service.OssService;
import cn.gransier.oss.utils.FileUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(tags = "云文件管理")
@Slf4j
@RestController
@RequestMapping("/ai-cloud/oss")
public class OssController {

    @Resource
    private OssService ossService;

    /**
     * 文件上传
     * 返回文件在 OSS 中的 objectName（或外链）
     *
     * @param file 上传的文件
     * @return 文件外链 URL 或 objectName
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        try {
            String objectName = FileUtils.getObjectName(file);
            // 上传到 OSS
            ossService.upload(objectName, file.getBytes(), FileUtils.getContentType(file));
            // 对象名称
            return objectName;
        } catch (IOException e) {
            log.error("文件读取失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }


    /**
     * 删除文件
     */
    @DeleteMapping("/")
    public void delete(@RequestParam("fileName") String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        ossService.delete(fileName);
    }

    /**
     * 获取文件信息（此处简化为返回外链；也可扩展为元数据）
     */
    @GetMapping("/info")
    public String getFileStatusInfo(@RequestParam("fileName") String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        // 可以扩展：检查是否存在、大小等，这里先返回 URL
        return ossService.getUrl(fileName);
    }

    /**
     * 获取预签名临时外链（推荐用于安全下载）
     * 有效期默认 1 小时（3600 秒）
     */
    @GetMapping("/url")
    public String getPreSignedObjectUrl(@RequestParam("fileName") String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        return ossService.getPresignedUrl(fileName, 3600);
    }

    /**
     * 文件下载（通过后端中转，适合私有文件）
     */
    @GetMapping("/download")
    public void download(@RequestParam("fileName") String fileName,
                         HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        try {
            FileUtils.doDownload(fileName, response, ossService.download(fileName));
        } catch (Exception e) {
            log.error("文件下载失败: {}", fileName, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("File download failed: " + e.getMessage());
        }
    }
}