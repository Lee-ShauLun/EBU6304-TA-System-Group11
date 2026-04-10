package cn.edu.bupt.tarecruitment;

import cn.edu.bupt.tarecruitment.config.AppConfig;
import cn.edu.bupt.tarecruitment.service.RecruitmentService;
import cn.edu.bupt.tarecruitment.store.XmlDataStore;
import cn.edu.bupt.tarecruitment.web.AppServer;
import java.io.IOException;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        AppConfig config = AppConfig.load();
        XmlDataStore dataStore = new XmlDataStore(config.getDataFile(), config.getUploadsDirectory());
        dataStore.initializeIfMissing();

        RecruitmentService recruitmentService =
                new RecruitmentService(dataStore, config.getUploadsDirectory());

        AppServer server = new AppServer(config, recruitmentService);
        server.start();
    }
}
