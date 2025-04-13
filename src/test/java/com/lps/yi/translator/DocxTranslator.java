package com.lps.yi.translator;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocxTranslator {

    private int num = 1;


    public static void main(String[] args) {
        try {
            DocxTranslator translator = new DocxTranslator();
            translator.extractDocumentContent("System Manual v0.1.docx", "transcript.docx");
            System.out.println("Translation completed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void extractDocumentContent(String inputPath, String outputPath) throws Exception {
        XWPFDocument doc = new XWPFDocument(Objects.requireNonNull(
                DocxTranslator.class.getClassLoader().getResourceAsStream(inputPath)));

        // 处理段落（包括标题和正文）
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            processParagraph(paragraph);
        }

        // 处理表格
        for (XWPFTable table : doc.getTables()) {
            processTable(table);
        }

        // 保存文档
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            doc.write(out);
        }
    }

    private void processParagraph(XWPFParagraph paragraph) {
        String originalText = paragraph.getText();
        if (StringUtils.hasText(originalText)) {
            // 正则匹配序号结构（如 1. / 1.1 / 1.1.1 后跟空格）
            Pattern pattern = Pattern.compile("^(\\d+(?:\\.\\d+)*\\s+)(.*)$");
            Matcher matcher = pattern.matcher(originalText);
            String newText;

            if (matcher.find()) {
                // 保留序号部分，替换后续内容为占位符
                String numberPart = matcher.group(1);
                newText = numberPart + "{{" + num++ + "}}";
            } else {
                // 无序号结构，整体替换
                newText = String.format("{{%s}}", num++);
            }
            setPlaceholder(paragraph, newText);
        }
    }

    private void processTable(XWPFTable table) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    processParagraph(paragraph);
                }
            }
        }
    }

    private void setPlaceholder(XWPFParagraph paragraph, String newText) {
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