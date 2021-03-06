package com.senior.cyber.dropbox;

import com.google.gson.Gson;
import com.senior.cyber.dropbox.configuration.ApplicationConfiguration;
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
import java.util.*;

@RestController
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private Gson gson;

    @Autowired
    private ApplicationConfiguration properties;

    @Autowired
    private OkHttpClient client;

    @RequestMapping(path = "/2/files/download", method = RequestMethod.POST)
    public ResponseEntity<Resource> download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String workspace = FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath(), false);

        String userAgent = request.getHeader("User-Agent");
        String range = request.getHeader("Range");
        ArgDto argDto = this.gson.fromJson(request.getHeader("Dropbox-API-Arg"), ArgDto.class);

        LOGGER.info("UserAgent [{}] [Range] [{}] [Path] [{}]", userAgent, range, argDto.getPath());

        String path = FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath() + argDto.getPath(), true);
        File file = new File(path);

        if (!path.startsWith(FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath(), true))) {
            throw new IllegalArgumentException();
        }

        if (!file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException();
        }

        Map<String, Object> dropboxApiResult = new LinkedHashMap<>();
        dropboxApiResult.put("name", file.getName());
        dropboxApiResult.put("path_lower", path.substring(workspace.length()));
        dropboxApiResult.put("path_display", path.substring(workspace.length()));
        dropboxApiResult.put("id", "id:" + UUID.randomUUID());
        dropboxApiResult.put("client_modified", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date()) + "Z");
        dropboxApiResult.put("server_modified", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date()) + "Z");
        dropboxApiResult.put("rev", System.currentTimeMillis() + "");
        dropboxApiResult.put("size", file.length());
        dropboxApiResult.put("is_downloadable", true);
        dropboxApiResult.put("content_hash", RandomStringUtils.randomAlphabetic(64));
        response.setHeader("Dropbox-Api-Result", this.gson.toJson(dropboxApiResult));

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
        String userAgent = request.getHeader("User-Agent");
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

        LOGGER.info("UserAgent [{}] [Size] [{}] [Path] [{}]", userAgent, file.length(), argDto.getPath());

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> json = new LinkedHashMap<>();
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
        String userAgent = request.getHeader("User-Agent");

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

        LOGGER.info("UserAgent [{}] [Size] [{}] [Path] [{}]", userAgent, file.length(), argDto.getPath());

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> json = new LinkedHashMap<>();
        {
            Map<String, Object> metadata = new LinkedHashMap<>();
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
        Map<String, Object> json = new LinkedHashMap<>();
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
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", UUID.randomUUID().toString());
        json.put("name", FilenameUtils.getName(argDto.getCommit().getPath()));
        json.put("client_modified", DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        json.put("server_modified", DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        json.put("rev", StringUtils.lowerCase(RandomStringUtils.randomNumeric(9)));
        json.put("size", destFile.length());
        this.gson.toJson(json, response.getWriter());
    }

    @RequestMapping(path = "/2/files/list_folder", method = RequestMethod.POST, produces = "application/json")
    public void listFolder(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ListFolderArg arg = this.gson.fromJson(IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8), ListFolderArg.class);

        String workspace = FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath(), false);

        String path = null;
        if (arg.getPath() == null || "".equals(arg.getPath())) {
            path = FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath(), true);
        } else {
            path = FilenameUtils.normalize(new File(this.properties.getWorkspace().getAbsolutePath(), arg.getPath()).getAbsolutePath(), true);
        }
        LOGGER.info("path [{}]", path);

        File pathFile = new File(path);

        Map<String, Object> json = new LinkedHashMap<>();

        List<Map<String, Object>> entries = new ArrayList<>();
        json.put("entries", entries);

        File[] items = pathFile.listFiles();

        if (items != null) {
            for (File item : items) {
                if (item.isDirectory()) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    String itemPath = FilenameUtils.normalizeNoEndSeparator(item.getAbsolutePath(), true);
                    entry.put(".tag", "folder");
                    entry.put("name", item.getName());
                    entry.put("path_lower", itemPath.substring(workspace.length()));
                    entry.put("path_display", itemPath.substring(workspace.length()));
                    entry.put("id", "id:" + UUID.randomUUID());
                    entries.add(entry);
                }
            }
            for (File item : items) {
                if (item.isFile()) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    String itemPath = FilenameUtils.normalizeNoEndSeparator(item.getAbsolutePath(), true);
                    entry.put(".tag", "file");
                    entry.put("name", item.getName());
                    entry.put("path_lower", itemPath.substring(workspace.length()));
                    entry.put("path_display", itemPath.substring(workspace.length()));
                    entry.put("id", "id:" + UUID.randomUUID());
                    entry.put("client_modified", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date()) + "Z");
                    entry.put("server_modified", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date()) + "Z");
                    entry.put("rev", System.currentTimeMillis() + "");
                    entry.put("size", item.length());
                    entry.put("is_downloadable", true);
                    entry.put("content_hash", RandomStringUtils.randomAlphabetic(64));
                    entries.add(entry);
                }
            }
        }
        json.put("cursor", UUID.randomUUID() + "");
        json.put("has_more", false);
        this.gson.toJson(json, response.getWriter());
    }

    @RequestMapping(path = "/2/files/move_v2", method = RequestMethod.POST, produces = "application/json")
    public void move(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RelocationArg arg = this.gson.fromJson(IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8), RelocationArg.class);

        String workspace = FilenameUtils.normalize(this.properties.getWorkspace().getAbsolutePath(), false);

        String fromPath = FilenameUtils.normalize(new File(this.properties.getWorkspace().getAbsolutePath(), arg.getFromPath()).getAbsolutePath(), true);
        String toPath = FilenameUtils.normalize(new File(this.properties.getWorkspace().getAbsolutePath(), arg.getToPath()).getAbsolutePath(), true);

        File fromPathFile = new File(fromPath);
        File toPathFile = new File(toPath);

        Map<String, Object> json = new LinkedHashMap<>();

        Map<String, Object> metadata = new LinkedHashMap<>();
        json.put("metadata", metadata);

        if (fromPathFile.isFile()) {
            FileUtils.moveFile(fromPathFile, toPathFile);
            String itemPath = FilenameUtils.normalizeNoEndSeparator(toPathFile.getAbsolutePath(), true);
            metadata.put(".tag", "file");
            metadata.put("name", toPathFile.getName());
            metadata.put("path_lower", itemPath.substring(workspace.length()));
            metadata.put("path_display", itemPath.substring(workspace.length()));
            metadata.put("id", "id:" + UUID.randomUUID());
            metadata.put("client_modified", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date()) + "Z");
            metadata.put("server_modified", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date()) + "Z");
            metadata.put("rev", System.currentTimeMillis() + "");
            metadata.put("size", toPathFile.length());
            metadata.put("is_downloadable", true);
            metadata.put("content_hash", RandomStringUtils.randomAlphabetic(64));
        }

        if (fromPathFile.isDirectory()) {
            FileUtils.moveFile(fromPathFile, toPathFile);
            String itemPath = FilenameUtils.normalizeNoEndSeparator(toPathFile.getAbsolutePath(), true);
            metadata.put(".tag", "folder");
            metadata.put("name", toPathFile.getName());
            metadata.put("path_lower", itemPath.substring(workspace.length()));
            metadata.put("path_display", itemPath.substring(workspace.length()));
            metadata.put("id", "id:" + UUID.randomUUID());
        }

        this.gson.toJson(json, response.getWriter());
    }

}
