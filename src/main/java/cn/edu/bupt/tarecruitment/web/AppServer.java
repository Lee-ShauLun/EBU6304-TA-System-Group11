package cn.edu.bupt.tarecruitment.web;

import cn.edu.bupt.tarecruitment.config.AppConfig;
import cn.edu.bupt.tarecruitment.service.RecruitmentService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class AppServer {

    private final AppConfig appConfig;
    private final HttpServer httpServer;

    public AppServer(AppConfig appConfig, RecruitmentService recruitmentService) throws IOException {
        this.appConfig = appConfig;
        this.httpServer = HttpServer.create(new InetSocketAddress(appConfig.getPort()), 0);
        this.httpServer.createContext("/", new RecruitmentHttpHandler(recruitmentService));
        this.httpServer.setExecutor(Executors.newFixedThreadPool(8));
    }

    public void start() {
        httpServer.start();
        System.out.println("TA Recruitment System started.");
        System.out.println("Open http://localhost:" + appConfig.getPort() + " in your browser.");
    }
}
