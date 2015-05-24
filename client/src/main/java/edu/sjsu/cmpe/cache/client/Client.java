package edu.sjsu.cmpe.cache.client;

public class Client {

    public static void main(String[] args) throws Exception {
        System.out.println("Start Client.");

        CRDTClass crdt = new CRDTClass();
        crdt.add("http://localhost:3000");
        crdt.add("http://localhost:3001");
        crdt.add("http://localhost:3002");


        //Read Repair
        crdt.writeFunction(1, "a");
        Thread.sleep(30 * 1000);
        crdt.writeFunction(1, "b");
        Thread.sleep(30 * 1000);
        System.out.println("all servers: " + crdt.readFunction(1));


        //Write Rollback
        Thread.sleep(30 * 1000);
        crdt.writeFunction(2, "c");
        System.out.println("End Client.");
    }

}
