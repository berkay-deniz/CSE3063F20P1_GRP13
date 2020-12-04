package com.data_labeling_system;

import java.io.IOException;

import com.data_labeling_system.util.DataLabelingSystem;
import org.apache.log4j.Logger;

public class App {
    static Logger logger=Logger.getLogger(App.class);
    public static void main(String[] args) throws IOException {
    	//DataLabelingSystem dataLabelingSystem = new DataLabelingSystem();
    	//dataLabelingSystem.startSystem();

        logger.error("hata!");
        logger.info("bilgi!");
        logger.trace("trace!");
        logger.warn("uyari!");
        logger.fatal("fatal!");
    }
}
