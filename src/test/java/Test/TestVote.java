package Test;

import common.java.httpServer.booter;
import common.java.nlogger.nlogger;

public class TestVote {
    public static void main(String[] args) {
        booter booter = new booter();
        try {
            System.out.println("Vote");
            System.setProperty("AppName", "Vote");
            booter.start(1007);
        } catch (Exception e) {
            nlogger.logout(e);
        }
    }
}
