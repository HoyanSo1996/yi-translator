package com.lps.yi.translator.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

/**
 * Class DocxService
 *
 * @author KennySo
 * @date 2025/4/12
 */
public interface DocxService {

    ByteArrayOutputStream translate(MultipartFile file);
}
