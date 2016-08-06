package org.dborm.web;


import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * 使用Jetty启动服务
 * 启动之后的默认访问地址:http://localhost:8000/dborm-web
 */
public class JettyServer {
    public static void main(String[] args) throws Exception {
        Server jettyServer = new Server();
        SocketConnector conn = new SocketConnector();
        conn.setPort(8000);//启动的服务器端口号
        jettyServer.setConnectors(new Connector[]{conn});

        WebAppContext wah = new WebAppContext();
        wah.setContextPath("/dborm-web");//项目名称
        wah.setWar("src/main/webapp");//web目录
        jettyServer.setHandler(wah);
        jettyServer.start();
    }


}
