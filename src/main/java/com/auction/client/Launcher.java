package com.auction.client;

// Class này KHÔNG extends Application, để chạy được từ IntelliJ
// mà không bị lỗi "JavaFX runtime components are missing".
public class Launcher {
    public static void main(String[] args) {
        ClientApp.main(args);
    }
}