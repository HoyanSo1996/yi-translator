package com.lps.yi.translator.controller;

import com.lps.yi.translator.service.DocxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

/**
 * Class DocumentController
 *
 * @author KennySo
 * @date 2025/4/12
 */
@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private DocxService docxService;

    @PostMapping("/translate")
    public ResponseEntity<byte[]> translate(@RequestPart("file") MultipartFile file) {
        ByteArrayOutputStream byteArrayOutputStream = docxService.translate(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", file.getOriginalFilename()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(byteArrayOutputStream.toByteArray());
    }

}
