import java.net.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;
import java.lang.Integer;
import java.lang.String;

public class PacketSender extends Thread {

    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;

    public PacketSender(String address, int port, String datagram) {
        try {
            socket = new Socket(address, port);
            System.out.println("__ Client is connected to server __ ");

            System.out.println("Datagram to send: "+ datagram);

            // SEND THE DATA //////////////////////////////////////////////////////////////////////////////////////////////////////
            // See this link: https://www.geeksforgeeks.org/socket-programming-in-java/



        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static String stringToHex(String str) {
        StringBuffer sb = new StringBuffer();

        char ch[] = str.toCharArray();
        for(int i = 0; i < ch.length; i++) {
            String hexString = Integer.toHexString(ch[i]);
            sb.append(hexString);
        }

        return sb.toString();
    }

    private static String ipToHex(String str) {

        StringBuffer sb = new StringBuffer();

        String[] words = str.split("\\.");

        for(int i = 0; i < words.length; i++) {
            int temp = Integer.parseInt(words[i]);
            String hexString = Integer.toHexString(temp);
            
            if( hexString.length() !=2) {
                hexString = "0"+hexString;
            }

            sb.append(hexString);
        }

        return sb.toString();

    }

    private static String createId(){

        int maxValue = 65535;           //== 0xFFFF
        Random r = new Random();
        int temp = r.nextInt(maxValue+1);
        String idField = Integer.toHexString(temp);

        if (idField.length()==1){
            return "000"+idField;
        }
        else if(idField.length()==2){
            return "00"+idField;
        }
        else if(idField.length()==3){
            return "0"+idField;
        }
        return idField;

    }

    private static String splitEveryFour(String str) {

        StringBuffer sb = new StringBuffer();

        char ch[] = str.toCharArray();

        for(int i = 0; i < ch.length; i++) {
            sb.append(ch[i]);
            if((i+1)%4==0) {
                sb.append(" ");
            }
        }

        return sb.toString();

    }
    private static String findLength(String str) {

        int length = str.length() +20;
        String l = Integer.toHexString(length);

        if (l.length()==1){
            return "000"+l;
        }
        else if(l.length()==2){
            return "00"+l;
        }
        else if(l.length()==3){
            return "0"+l;
        }
        return l;
    }

    private static String calculateChecksum(String str) {

        str = splitEveryFour(str);
        String[] words = str.split(" ");

        int sum = 0;

        for (int i=0; i<words.length; i++) {
            int temp = Integer.parseInt(words[i], 16);
            sum +=temp;
        }
        
        String checksum = Integer.toHexString(sum);

        //WRAPPING THE SUM
        if (checksum.length()!=4) {

            int first = checksum.charAt(0);         //first number
            checksum = checksum.substring(1);       // rest of the checksum

            sum = Integer.parseInt(checksum, 16)+first;   //add em

        }

        sum = 65535 - sum;

        return Integer.toHexString(sum);                //conver to HEX
    }

    private static String encodeMessage(String cIp, String sIp, String pl) {

        String idField = createId();

        String clientIP = ipToHex(cIp);
        
        String serverIP = ipToHex(sIp);
        
        String payload = stringToHex(pl);

        String length = findLength(pl);

        String checksum = calculateChecksum("4500"+length+idField+"40004006"+clientIP+serverIP);          ///LAST STEP: CALCULATE CHECKSUM

        String datagram = "4500"+length+idField+"40004006"+checksum+clientIP+serverIP+payload;

        return splitEveryFour(datagram);

    }

    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // GET THE SERVER IP AND THE PAYLOAD
        System.out.println("_______________________________________________________");
        System.out.println("Enter Server IP:    ");
        String serverIp = br.readLine();
        System.out.println("Enter Payload:      ");
        String payload = br.readLine();
        System.out.println("_______________________________________________________");

        String clientIp = InetAddress.getLocalHost().getHostAddress();

        String datagram = encodeMessage(clientIp, serverIp, payload);

        PacketSender pacSend = new PacketSender("localhost", 5000, datagram);

    }
};