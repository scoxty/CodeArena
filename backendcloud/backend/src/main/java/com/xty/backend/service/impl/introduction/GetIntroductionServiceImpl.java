package com.xty.backend.service.impl.introduction;

import com.xty.backend.service.introduction.GetIntroductionService;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class GetIntroductionServiceImpl implements GetIntroductionService {
    @Override
    public String getIntroduction() {
        String readmePath = "AboutCodeArena.md";
        try {
            return Files.readString(Paths.get(readmePath));
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading file: " + e.getMessage();
        }
    }
}
