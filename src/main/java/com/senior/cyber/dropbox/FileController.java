package com.senior.cyber.dropbox;

import com.senior.cyber.dropbox.configuration.ApplicationConfiguration;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private Gson gson;

    @Autowired
    private ApplicationConfiguration properties;

    @Autowired
    private OkHttpClient client;

    @RequestMapping(path = "/2/files/download", method = RequestMethod.POST, produces = "application/octet-stream")
    public ResponseEntity<Resource> download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorization = request.getHeader("Authorization");
        String userAgent = request.getHeader("User-Agent");
        String range = request.getHeader("Range");
        String token = authorization.substring(7);
        ArgDto argDto = this.gson.fromJson(request.getHeader("Dropbox-API-Arg"), ArgDto.class);

        LOGGER.info("UserAgent [{}] Token [{}] [Range] [{}] [Path] [{}]", userAgent, token, range, argDto.getPath());

        String path = FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath() + "/" + argDto.getPath(), true);
        File file = new File(path);

        if (!path.startsWith(FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath(), true))) {
            throw new IllegalArgumentException();
        }

        if (!file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException();
        }

        FileSystemResource resource = new FileSystemResource(file);
        String extension = FilenameUtils.getExtension(file.getName());
        if ("jpg".equalsIgnoreCase(extension)) {
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.IMAGE_JPEG).body(resource);
        } else if ("png".equalsIgnoreCase(extension)) {
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.IMAGE_PNG).body(resource);
        } else if ("mp4".equalsIgnoreCase(extension)) {
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.parseMediaType("video/mp4")).body(resource);
        } else if ("m4a".equalsIgnoreCase(extension)) {
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.parseMediaType("audio/mp4")).body(resource);
        } else if ("m3u8".equalsIgnoreCase(extension)) {
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.parseMediaType("application/x-mpegURL")).body(resource);
        } else if ("ts".equalsIgnoreCase(extension)) {
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.parseMediaType("video/mp2t")).body(resource);
        } else if ("vtt".equalsIgnoreCase(extension)) {
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.parseMediaType("text/vtt")).body(resource);
        } else if ("srt".equalsIgnoreCase(extension)) {
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.parseMediaType("text/plain")).body(resource);
        } else if ("mp3".equalsIgnoreCase(extension)) {
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.parseMediaType("audio/mp3")).body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
        }
    }

    @RequestMapping(path = "/2/files/upload", method = RequestMethod.POST, produces = "application/json")
    public void upload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorization = request.getHeader("Authorization");
        String userAgent = request.getHeader("User-Agent");
        String token = authorization.substring(7);
        ArgDto argDto = this.gson.fromJson(request.getHeader("Dropbox-API-Arg"), ArgDto.class);

        String path = FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath() + "/" + argDto.getPath(), true);

        if (!path.startsWith(FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath(), true))) {
            throw new IllegalArgumentException();
        }

        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            throw new IllegalArgumentException();
        }

        file.getParentFile().mkdirs();

        long size = 0;
        try (FileOutputStream stream = FileUtils.openOutputStream(file)) {
            IOUtils.copy(request.getInputStream(), stream);
        }

        LOGGER.info("UserAgent [{}] Token [{}] [Size] [{}] [Path] [{}]", userAgent, token, file.length(), argDto.getPath());

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> json = new HashMap<>();
        json.put("id", UUID.randomUUID().toString());
        json.put("name", FilenameUtils.getName(argDto.getPath()));
        json.put("client_modified", DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        json.put("server_modified", DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        json.put("rev", StringUtils.lowerCase(RandomStringUtils.randomNumeric(9)));
        json.put("size", size);
        this.gson.toJson(json, response.getWriter());
    }

    @RequestMapping(path = "/2/files/delete_v2", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void deleteV2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorization = request.getHeader("Authorization");
        String userAgent = request.getHeader("User-Agent");
        String token = authorization.substring(7);

        ArgDto argDto = this.gson.fromJson(IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8), ArgDto.class);
        String path = FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath() + "/" + argDto.getPath(), true);

        if (!path.startsWith(FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath(), true))) {
            throw new IllegalArgumentException();
        }
        File file = new File(path);
        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
        } else if (file.isFile()) {
            FileUtils.deleteQuietly(file);
        }

        LOGGER.info("UserAgent [{}] Token [{}] [Size] [{}] [Path] [{}]", userAgent, token, file.length(), argDto.getPath());

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> json = new HashMap<>();
        {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("name", FilenameUtils.getName(argDto.getPath()));
            metadata.put("path_lower", argDto.getPath());
            metadata.put("path_display", argDto.getPath());
            metadata.put("parent_shared_folder_id", UUID.randomUUID().toString());
            json.put("metadata", metadata);
        }

        this.gson.toJson(json, response.getWriter());
    }

    @RequestMapping(path = "/2/files/upload_session/start", method = RequestMethod.POST, consumes = "application/octet-stream", produces = "application/json")
    public void uploadSessionStart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uuid = UUID.randomUUID().toString();
        LOGGER.info("session [{}]", uuid);
        File tempFile = new File(FileUtils.getTempDirectory(), uuid);
        try (OutputStream stream = FileUtils.openOutputStream(tempFile)) {
            IOUtils.copy(request.getInputStream(), stream);
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> json = new HashMap<>();
        json.put("session_id", uuid);
        this.gson.toJson(json, response.getWriter());
    }

    @RequestMapping(path = "/2/files/upload_session/append_v2", method = RequestMethod.POST, consumes = "application/octet-stream", produces = "application/json")
    public void uploadSessionAppendV2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ArgDto argDto = this.gson.fromJson(request.getHeader("Dropbox-API-Arg"), ArgDto.class);
        LOGGER.info("        [{}] offset [{}]", argDto.getCursor().getSessionId(), argDto.getCursor().getOffset());
        File tempFile = new File(FileUtils.getTempDirectory(), argDto.getCursor().getSessionId());
        try (OutputStream stream = FileUtils.openOutputStream(tempFile, true)) {
            IOUtils.copy(request.getInputStream(), stream);
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        this.gson.toJson(null, response.getWriter());
    }

    @RequestMapping(path = "/2/files/upload_session/finish", method = RequestMethod.POST, consumes = "application/octet-stream", produces = "application/json")
    public void uploadSessionFinish(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ArgDto argDto = this.gson.fromJson(request.getHeader("Dropbox-API-Arg"), ArgDto.class);
        File tempFile = new File(FileUtils.getTempDirectory(), argDto.getCursor().getSessionId());
        try (OutputStream stream = FileUtils.openOutputStream(tempFile, true)) {
            IOUtils.copy(request.getInputStream(), stream);
        }


        String path = FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath() + "/" + argDto.getCommit().getPath(), true);

        if (!path.startsWith(FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath(), true))) {
            throw new IllegalArgumentException();
        }

        File destFile = new File(path);

        FileUtils.moveFile(tempFile, destFile);

        LOGGER.info("        [{}] path [{}]", argDto.getCursor().getSessionId(), argDto.getCommit().getPath());

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> json = new HashMap<>();
        json.put("id", UUID.randomUUID().toString());
        json.put("name", FilenameUtils.getName(argDto.getCommit().getPath()));
        json.put("client_modified", DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        json.put("server_modified", DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        json.put("rev", StringUtils.lowerCase(RandomStringUtils.randomNumeric(9)));
        json.put("size", destFile.length());
        this.gson.toJson(json, response.getWriter());
    }

}
