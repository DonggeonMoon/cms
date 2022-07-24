package egovframework.com.cms.index.service.impl;

import egovframework.com.cms.index.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IndexServiceImpl implements IndexService {
    @Override
    public void dummy() {

    }

    @Override
    public void flush() {
        log.info("IndexServiceImpl is doing dummy job...");
    }
}
