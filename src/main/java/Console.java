import com.sun.enterprise.ee.cms.core.*;
import jline.ConsoleReader;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA.
 * User: falcao
 * Date: Dec 29, 2009
 * Time: 8:13:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class Console {
    public static void main(String[] args) throws IOException {

        new Console();

//        Terminal t = Terminal.setupTerminal();
//
//            public int complete(String s, int i, List list) {
//                System.out.println("s" + s);
//                System.out.println("i" + i);
//                System.out.println(list);
//
//                return 1;
//            }
//        });


    }


    public Console() {
        GroupManagementService gms = (GroupManagementService)
                GMSFactory.startGMSModule("console", "Group1", GroupManagementService.MemberType.SPECTATOR, null);

        try {
            gms.join();
        } catch (GMSException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ConsoleReader cr = null;
        try {
            cr = new ConsoleReader();
//        cr.addCompletor(new Completor() {

            boolean running = true;
            while (running) {
                String line = cr.readLine("# ");
                if ("nodes".equals(line)) {
                    String leader = gms.getGroupHandle().getGroupLeader();
                    System.out.println("leader:" + leader);
                    List<String> coreMembers = gms.getGroupHandle().getCurrentCoreMembers();
                    for (String member : coreMembers) {
                        if (leader.equals(member)) {
                            System.out.println(member + " (leader)");
                        } else {
                            System.out.println(member);
                        }
                    }

                } else if ("add".equals(line)) {
                    GroupHandle gh = gms.getGroupHandle();

                    String leader = gh.getGroupLeader();
                    System.out.println("sending message to leader " + leader);
//                    gh.sendMessage(leader, );
                    try {
                        gh.sendMessage(leader, "SimpleSampleComponent",
                                "Add".getBytes());
                    } catch (GMSException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                } else if ("cache".equals(line)) {
                    DistributedStateCache dsc = gms.getGroupHandle().getDistributedStateCache();

                    System.out.println("caches");
                    //Hangs!      
                    Map<Serializable, Serializable> caches = dsc.getAllCache();
                    for (Map.Entry<Serializable, Serializable> entry : caches.entrySet()) {
                        System.out.println((String) entry.getKey() + "=" + (String) entry.getValue());
                    }


                }

//            System.out.println("line" + line);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }
}
