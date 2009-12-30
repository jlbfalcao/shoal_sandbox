import com.sun.enterprise.ee.cms.core.*;
import com.sun.enterprise.ee.cms.impl.client.*;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Simple sample client application that uses Shoal library to register for
 * group events, join a pre-defined group,  get notifications of group events,
 * send/receive messages, and leave the group.
 * To see Shoal functionality, run this example in two or more terminals or
 * machines on the same subnet. See the application server demo located in
 * the Shoal GMS tests source code to see how cross subnet settings are made.
 */


public class Worker implements CallBack {
    final static Logger logger = Logger.getLogger("SimpleShoalGMSSample");
    final Object waitLock = new Object();
    GroupManagementService gms;

    public static void main(String[] args) {
        Worker sgs = new Worker();
        try {
            sgs.runSimpleSample();
        } catch (GMSException e) {
            logger.log(Level.SEVERE, "Exception occured while joining group:" + e);
        }
    }

    /**
     * Runs this sample
     *
     * @throws GMSException
     */
    private void runSimpleSample() throws GMSException {
        logger.log(Level.INFO, "Starting SimpleShoalGMSSample....");

        final String serverName = "server" + System.currentTimeMillis();
        final String groupName = "Group1";

        //initialize Group Management Service
        gms = initializeGMS(serverName, groupName);

        //register for Group Events
        registerForGroupEvents(gms);


        //join group
        joinGMSGroup(groupName, gms);

//        gms.
        DistributedStateCache dsc = gms.getGroupHandle().getDistributedStateCache();


//        dsc.
        System.out.println("dsc:" + dsc.toString());
        logger.log(Level.INFO, "memberType:" + gms.getMemberType().toString());

        boolean running = true;
        while (running) {
            try {
                Thread.sleep(5000L);

                System.out.println(dsc.getAllCache());

                System.out.println("Running " + serverName);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.exit(0);
            }
        }


        try {
            //send some messages
            sendMessages(gms, serverName);
            waitForShutdown();

        } catch (InterruptedException e) {
            logger.log(Level.WARNING, e.getMessage());
        }
        //leave the group gracefully
        leaveGroupAndShutdown(serverName, gms);
    }

    private GroupManagementService initializeGMS(String serverName, String groupName) {
        logger.log(Level.INFO, "Initializing Shoal for member: " + serverName + " group:" + groupName);
        return (GroupManagementService) GMSFactory.startGMSModule(serverName,
                groupName, GroupManagementService.MemberType.CORE, null);
    }

    private void registerForGroupEvents(GroupManagementService gms) {
        logger.log(Level.INFO, "Registering for group event notifications");
        gms.addActionFactory(new JoinNotificationActionFactoryImpl(this));
        gms.addActionFactory(new FailureSuspectedActionFactoryImpl(this));
        gms.addActionFactory(new FailureNotificationActionFactoryImpl(this));
        gms.addActionFactory(new PlannedShutdownActionFactoryImpl(this));
        gms.addActionFactory(new MessageActionFactoryImpl(this), "SimpleSampleComponent");


        gms.addActionFactory(new GroupLeadershipNotificationActionFactoryImpl(this));
    }

    private void joinGMSGroup(String groupName, GroupManagementService gms) throws GMSException {
        logger.log(Level.INFO, "Joining Group " + groupName);
        gms.join();
    }

    private void sendMessages(GroupManagementService gms, String serverName) throws InterruptedException, GMSException {
        logger.log(Level.INFO, "wait 15 secs to send 10 messages");
        synchronized (waitLock) {
            waitLock.wait(10000);
        }
        GroupHandle gh = gms.getGroupHandle();

        logger.log(Level.INFO, "Sending messages...");
        for (int i = 0; i <= 10; i++) {
            gh.sendMessage("SimpleSampleComponent",
                    MessageFormat.format("Message {0}from server {1}", i, serverName).getBytes());
        }
    }

    private void waitForShutdown() throws InterruptedException {
        logger.log(Level.INFO, "wait 20 secs to shutdown");
        synchronized (waitLock) {
            waitLock.wait(20000);
        }
    }

    private void leaveGroupAndShutdown(String serverName, GroupManagementService gms) {
        logger.log(Level.INFO, "Shutting down instance " + serverName);
        gms.shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
        System.exit(0);
    }

    public void processNotification(Signal signal) {
        logger.log(Level.INFO, "Received Notification of type : " + signal.getClass().getName());
        try {
            signal.acquire();
            logger.log(Level.INFO, "Source Member: " + signal.getMemberToken());
            if (signal instanceof MessageSignal) {
                logger.log(Level.INFO, "Message: " + new String(((MessageSignal) signal).getMessage()));
                DistributedStateCache dsc = gms.getGroupHandle().getDistributedStateCache();

                // definir quem é que vai receber o job:
//                GroupHandle gh = gms.getGroupHandle();
//                List<String> members = gh.getCurrentAliveOrReadyMembers();
//                int p = (int)(Math.random() * (members.size()));
//                String member = members.get(p);

                dsc.addToCache("caches", signal.getMemberToken(), "1", "work");

            }

//        dsc.addToCache("queries", serverName, "01", "23");


            signal.release();


        } catch ( GMSException e ) {
            e.printStackTrace();
        } catch (SignalAcquireException e) {
            logger.log(Level.WARNING, "Exception occured while acquiring signal" + e);
        } catch (SignalReleaseException e) {
            logger.log(Level.WARNING, "Exception occured while releasing signal" + e);
        }

    }
}
