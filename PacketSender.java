import java.net.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;
import java.lang.Integer;
import java.lang.String;

public class PacketSender extends Thread {

    private Socket socket = null;
    private DataOutputStream out = null;

    public PacketSender(String address, int port, String datagram) {
        try {
            socket = new Socket(address, port);
            System.out.println("__ Client is connected to server __ ");

            System.out.println("Datagram to send (with padding): "+ datagram);

            // SEND THE DATA //////////////////////////////////////////////////////////////////////////////////////////////////////
            // See this link: https://www.geeksforgeeks.org/socket-programming-in-java/
            out = new DataOutputStream (socket.getOutputStream());
            out.writeUTF(datagram);


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
            //System.out.println("i= "+i+" : "+Integer.toHexString(sum));
        }
        
        String checksum = Integer.toHexString(sum);
        //System.out.println(checksum);
        
        //WRAPPING THE SUM
        if (checksum.length()!=4) {

            //int first = checksum.charAt(0);         //first number
            String first2 = checksum.substring(0,1);
            //System.out.println(first);
            checksum = checksum.substring(1);       // rest of the checksum
            //System.out.println(checksum);
            sum = Integer.parseInt(checksum, 16)+Integer.parseInt(first2, 16);   //add em
            //System.out.println(sum);
        }

        sum = 65535 - sum;
        //System.out.println(sum);

        return Integer.toHexString(sum);                //conver to HEX
    }

    private static String addPadding(String str) {
        
        while (str.length() % 8 != 0) {
            str=str+"0";
        }
        return str;
    }

    private static String encodeMessage(String cIp, String sIp, String pl) {

        String idField = createId();

        String clientIP = ipToHex(cIp);
        
        String serverIP = ipToHex(sIp);
        
        String payload = stringToHex(pl);

        String length = findLength(pl);

        String checksum = calculateChecksum("4500"+length+idField+"40004006"+clientIP+serverIP);          ///LAST STEP: CALCULATE CHECKSUM

        String datagram = "4500"+length+idField+"40004006"+checksum+clientIP+serverIP+addPadding(payload);

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