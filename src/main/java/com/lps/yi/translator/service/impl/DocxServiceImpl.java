package com.lps.yi.translator.service.impl;

import com.lps.yi.translator.service.DocxService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class DocxServiceImpl
 *
 * @author KennySo
 * @date 2025/4/12
 */
@Slf4j
@Service("docxService")
public class DocxServiceImpl implements DocxService {


    @Override
    public ByteArrayOutputStream translate(MultipartFile file) {
        try {
            int num = 1;
            XWPFDocument doc = new XWPFDocument(file.getInputStream());

            // 处理段落
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                num = processParagraph(paragraph, num);
            }

            // 处理表格
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            num = processParagraph(paragraph, num);
                        }
                    }
                }
            }

            // 输出文档
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("文档解析异常.");
        }
    }

    private int processParagraph(XWPFParagraph paragraph, int currentNum) {
        String originalText = paragraph.getText();
        if (!StringUtils.hasText(originalText)) {
            return currentNum;
        }

        // 获取除了标题以外的文本
        Pattern pattern = Pattern.compile("^(\\d+(?:\\.\\d+)*\\s+)(.*)$");
        Matcher matcher = pattern.matcher(originalText);
        String newText;

        if (matcher.find()) {
            String numberPart = matcher.group(1);
            // contentToStore = matcher.group(2);
            newText = numberPart + "{{" + currentNum + "}}";

        } else {
            // contentToStore = originalText;
            newText = "{{" + currentNum + "}}";

        }

        processPlaceholder(paragraph, newText);
        return currentNum + 1;
    }

    private void processPlaceholder(XWPFParagraph paragraph, String newText) {
        CTP ctp = paragraph.getCTP();
        List<CTR> runs = ctp.getRList();
        // 保留段落级样式（自动保持）

        // 获取第一个文本运行的样式（如果有的话）
        CTRPr copyStyle = null;
        if (!runs.isEmpty()) {
            CTR firstRun = runs.get(0);
            if (firstRun.isSetRPr()) {
                copyStyle = (CTRPr) firstRun.getRPr().copy(); // 深拷贝样式
            }
        }
        // 清空所有现有文本样式
        ctp.getRList().clear();
        // 创建新文本运行
        CTR newRun = ctp.addNewR();
        // 应用复制的样式
        if (copyStyle != null) {
            newRun.setRPr(copyStyle);
        }
        // 设置新文本
        CTText newTextNode = newRun.addNewT();
        newTextNode.setStringValue(newText);
    }
}
