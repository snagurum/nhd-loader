package com.nhd.batch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhd.models.Holiday;
import com.nhd.models.JobStatus;
import com.nhd.service.AuditService;
import com.nhd.service.StockService;
import com.nhd.util.JobName;
import com.web.crawler.Bot;
import com.web.crawler.BotUtil;
import com.web.crawler.Page;
import com.web.crawler.PageLoadException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.SerializationUtils;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@AllArgsConstructor
public class HolidayRunner {
    private static final Logger log = LoggerFactory.getLogger(HolidayRunner.class);

    private static final Bot HOLIDAY_BOT;

    static {        
        Yaml yaml = new Yaml(new Constructor(Bot.class, new LoaderOptions()));
        InputStream inputStream = BspRunner.class.getClassLoader().getResourceAsStream("yamlConfigs/holidays.yaml");
         HOLIDAY_BOT = yaml.load(inputStream);
        log.info(HOLIDAY_BOT.toString());       
    }
    

    private AuditService auditService ;

    private StockService stockService ;

    private ObjectMapper mapper;;
    
    @Scheduled(cron="#{${loader.holday.scheduler.cron}}")
    public void runJob(){

        JobStatus audit = auditService.startJob(JobName.HOLIDAY);
        try {
            Bot holidayBot = runJob0();
            List<Holiday> holidays = buildObjects(holidayBot);
            stockService.saveAllHolidays(holidays);

        } catch (PageLoadException | IOException | InterruptedException e) {
                log.error(null, e);
                auditService.failJob(audit);
        }

        auditService.endJob(audit);
    }

    public List<Holiday> buildObjects(Bot bot) throws IOException {
        List<Holiday> holidays = new ArrayList<>();

        for (Page page : bot.getPages()) {
            if(page.getPersistantData()){
                JsonNode rootNode = mapper.readTree(page.getResponseData());
                JsonNode cmNode = rootNode.get("CM");
                if (cmNode != null && cmNode.isArray()) {
                    List<Holiday> tempHolidays = mapper.convertValue(cmNode, new TypeReference<List<Holiday>>() {});                 
                    holidays.addAll(tempHolidays);             
                }
            }
        }
        
        return holidays;
    }

    private Bot runJob0() throws PageLoadException, IOException, InterruptedException{
        Bot holidayBot = SerializationUtils.clone(HOLIDAY_BOT);
        Map<String, String> gParam = new HashMap<>();
        BotUtil botUtil = new BotUtil(holidayBot, gParam);
        botUtil.process();
        return holidayBot;
    }
}
