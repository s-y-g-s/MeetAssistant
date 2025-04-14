package com.treemoon.MeetAssist.service;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.treemoon.MeetAssist.dto.DataJson;
import com.treemoon.MeetAssist.mapper.EnhanceTransMapper;
import com.treemoon.MeetAssist.mapper.TranscriptionMapper;
//import com.treemoon.MeetAssist.pojo.EnhanceTrans;
import com.treemoon.MeetAssist.pojo.Transcription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransDataService {

    private final EnhanceTransMapper enhanceTransMapper;
    private final TranscriptionMapper transcriptionMapper;

    public void getProcessedData(){
        List<Transcription> rawData = enhanceTransMapper.getJson();

        List<Transcription> dataJsons1 = new ArrayList<>();
        List<Transcription> dataJsons2 = new ArrayList<>();
        StringBuilder all_text= new StringBuilder();

        for (int i = 0; i < rawData.size(); i++) {
            Transcription et = rawData.get(i);
            Transcription mc1 = new Transcription();
            Transcription mc2 = new Transcription();

            mc1.setIndex(et.getIndex());
            mc1.setText(et.getText());
            mc1.setBeginTime(et.getBeginTime());

            mc2.setIndex(et.getIndex());
            mc2.setText(et.getText());
            mc2.setBeginTime(et.getBeginTime());
            mc2.setCard(et.getCard());

            all_text.append(et.getText());

            if (i + 1 < rawData.size()) {
                mc1.setEndTime(rawData.get(i + 1).getBeginTime());
                mc2.setEndTime(rawData.get(i + 1).getBeginTime());
            } else {
                mc1.setEndTime(mc1.getBeginTime() + 10000); // 示例值
                mc2.setEndTime(mc1.getBeginTime() + 10000); // 示例值
            }

            dataJsons1.add(mc1);
            dataJsons2.add(mc2);
        }

        // 保存为 JSON 文件
        saveAsJsonFile(dataJsons1, "D://enhance_output_nocards.json");
        saveAsJsonFile(dataJsons2, "D://enhance_output_cards.json");

        try(FileWriter writer = new FileWriter("D://enhance_all_text.txt")){
            writer.write(String.valueOf(all_text));
        } catch (IOException e) {
            System.out.println("保存发生错误: " + e.getMessage());
        }

    }

    public void getRawData(){
        List<Transcription> rawData=transcriptionMapper.getAll();
        List<Transcription> dataJsons=new ArrayList<>();

        for (int i = 0; i < rawData.size(); i++) {
            Transcription et = rawData.get(i);
            Transcription mc1 = new Transcription();

            mc1.setIndex(et.getIndex());
            mc1.setText(et.getText());
            mc1.setBeginTime(et.getBeginTime());


            if (i + 1 < rawData.size()) {
                mc1.setEndTime(rawData.get(i + 1).getBeginTime());
            } else {
                mc1.setEndTime(mc1.getBeginTime() + 10000); // 示例值
            }

            dataJsons.add(mc1);
        }
        // 保存为 JSON 文件
        saveAsJsonFile(dataJsons, "D://raw_output.json");
    }

    private void saveAsJsonFile(List<Transcription> data, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // 将集合序列化为 JSON 字符串并写入文件
            objectMapper.writeValue(new File(filePath), data);
            System.out.println("JSON 文件保存成功：" + filePath);
        } catch (IOException e) {
            System.err.println("保存 JSON 文件时出错：" + e.getMessage());
        }
    }
}