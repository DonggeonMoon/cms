package egovframework.com.cms.media.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@Slf4j
public class FFmpegMetadata {
    private long duration;

    public FFmpegMetadata(String logFilePath) {
        //long playTime = 0L;
        File logFile = new File(logFilePath);
        try {
            List<String> lines = FileUtils.readLines(logFile, "UTF-8");
            for (String line : lines) {
                log.info("[ECMS] FFmpegMetadata {}", line);
                //라인 파싱
                if (line.contains("Duration")) {
                    Pattern p = Pattern.compile("((\\d{2}):(\\d{2}):(\\d{2}))");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        long hour = Long.parseLong(m.group(2));
                        long min = Long.parseLong(m.group(3));
                        long sec = Long.parseLong(m.group(4));
                        this.duration = hour * 60 * 60 + min * 60 + sec + 1;//마지막 +1 소수점 이하 반영분
                        log.info("[ECMS] FFmpeg Video Play Time : {} of File : {}", m.group(1), FilenameUtils.getName(logFilePath));
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
