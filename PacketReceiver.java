import java.net.*;
import java.io.*;
import java.lang.Integer;
import java.lang.String;

public class PacketReceiver extends Thread {

    private ServerSocket server = null;
    private Socket socket = null;
    private DataInputStream in = null;

    public PacketReceiver(int port){
        try {
            server = new ServerSocket(port);
            System.out.println("__ Server is started. Waiting for client ___ ");

            socket = server.accept();
            System.out.println("__ Client accepted __ ");
            System.out.println("_______________________________________________________");

            // RECEIVE THE DATA  //////////////////////////////////////////////////////////////////////////////////////////////////////
            // See this link: https://www.geeksforgeeks.org/socket-programming-in-java/
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            String datagram = in.readUTF();
            System.out.println("Datagram received: "+datagram);
            datagram = removePadding(datagram);
            System.out.println("Datagram received (without padding): "+datagram);
            System.out.println("_______________________________________________________");
            getMessage(datagram);

        
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static String removePadding(String str) {

        String[] strs = str.split(" ");
        
        int lengthIPdec = Integer.parseInt(strs[1],16);

        str = str.substring(0,(lengthIPdec*2)+(lengthIPdec/2));
        System.out.println(str);
        return str;
    }

    public static boolean verifyChecksum(String head, String lengthIP, String idField, String flags, String tcp, String checksum, String ipsource, String ipdest){

        //split ips
        String ipsource1 = ipsource.substring(0,4);
        String ipsource2 = ipsource.substring(5);
        String ipdest1 = ipdest.substring(0,4);
        String ipdest2 = ipdest.substring(5);

        System.out.println(ipsource1);
        System.out.println(ipsource2);
        System.out.println(ipdest1);
        System.out.println(ipdest2);


        int headDec = Integer.parseInt(head,16);
        System.out.println(headDec);
        int lengthIPdec = Integer.parseInt(lengthIP,16);
        int idFieldDec = Integer.parseInt(idField,16);
        int flagsDec = Integer.parseInt(flags,16);
        int tcpDec = Integer.parseInt(tcp,16);
        int checksumDec = Integer.parseInt(checksum,16);
        int ipsourceDec1 = Integer.parseInt(ipsource1,16);
        int ipsourceDec2 = Integer.parseInt(ipsource2,16);
        int ipdestDec1 = Integer.parseInt(ipdest1,16);
        int ipdestDec2 = Integer.parseInt(ipdest2,16);
        
        int sum = headDec+lengthIPdec+idFieldDec+flagsDec+tcpDec+checksumDec+ipsourceDec1+ipsourceDec2+ipdestDec1+ipdestDec2;

        String sumHex = Integer.toHexString(sum);
        System.out.println(sumHex);
        if (sumHex.length() > 4){
            String carry = sumHex.substring(0,1);
            sumHex = sumHex.substring(1);
            int carryDec = Integer.parseInt(carry,16);
            int sumHexDec = Integer.parseInt(sumHex,16);
            sum = carryDec+sumHexDec;
            sumHex = Integer.toHexString(sum);
        }

        System.out.println(sumHex);
        if (sumHex.equals("ffff")){
            return true;
        }
        return false;
    }

    public static String getIP(String ip){

        int num1 = Integer.parseInt(ip.substring(0,2),16);
        int num2 = Integer.parseInt(ip.substring(2,4),16);
        int num3 = Integer.parseInt(ip.substring(5,7),16);
        int num4 = Integer.parseInt(ip.substring(7),16);

        // System.out.println(num1);
        // System.out.println(num2);
        // System.out.println(num3);
        // System.out.println(num4);

        return num1+"."+num2+"."+num3+"."+num4;
    }

    public static String getText(String hex){
        StringBuilder sb = new StringBuilder("");
        for (int i=0; i< hex.length(); i += 2){
            String character = hex.substring(i, i+2);
            sb.append((char) Integer.parseInt(character,16));
        }
        return sb.toString();
    }


    public static void getMessage(String stream){

        String[] myStream = stream.split(" ");

        String head = myStream[0];
        String lengthIP = myStream[1];
        String idField = myStream[2];
        String flags = myStream[3];
        String tcp = myStream[4];
        String checksum = myStream[5];
        String ipsource = myStream[6]+" "+myStream[7];
        String ipdest = myStream[8]+" "+myStream[9];
        //String[] message = Arrays.copyOfRange(myStream, 10, myStream.length);

        String message = "";
        for (int i=10; i<myStream.length; i++){
            message += myStream[i];
        }
        //System.out.println(message);

        // System.out.println(head);
        // System.out.println(lengthIP);
        // System.out.println(idField);
        // System.out.println(flags);
        // System.out.println(tcp);
        // System.out.println(checksum);
        // System.out.println(ipsource);
        // System.out.println(ipdest);
        // System.out.println(message);

        // Verify the checksum
        boolean check = verifyChecksum(head, lengthIP, idField, flags, tcp, checksum, ipsource, ipdest);
        //System.out.println(check);
        if (!check){
            System.out.println("The verification of the checksum demonstrates that the packet received is corrupted. Packet discarded!");
        } else {
            // Find the IP addresses
            String ipsourceWord = getIP(ipsource);
            String ipdestWord = getIP(ipdest);

            // System.out.println(ipsourceWord);
            // System.out.println(ipdestWord);

            // Find payload and length of the packet
            int lengthPacket = Integer.parseInt(lengthIP.substring(2,4),16);
            int payload = Integer.parseInt(lengthIP.substring(0,2),16)+20;

            // System.out.println(payload);
            // System.out.println(lengthPacket);

            // Find the message
            String decodedMessage = getText(message);
            //System.out.println(decodedMessage);
            
            System.out.println("Receives the data stream and prints to the screen the data received with the following message:");
            System.out.println("The data received from "+ipsourceWord+" is "+decodedMessage);
            System.out.println("The data has "+(8*payload)+" bites or "+payload+" bytes. Total length of the packet is "+lengthPacket+" bytes.");
            System.out.println("The verification of the checksum demonstrates that the packet received is correct.");
        }
    }

    public static void main(String[] args) throws Exception {

        PacketReceiver pacRec = new PacketReceiver(5000);

        //getMessage("4500 0028 1c46 4000 4006 9D34 C0A8 0003 C0A8 0001 434f 4c4f 4d42 4941 2032 202d 204d 4553 5349 2030");

        //4500 0028 e7a5 4000 4006 cf8d c0a8 021b c0a8 0001 434f 4c4f 4d42 4945 2032 202d 204d 4553 5349 2030 

        //4500[fixed] 0028[length IP] 1c46[ID field] 4000[fixed] 4006[fixed] 9D35[checksum] C0A8 0003 [IP source] C0A8 0001 [IP destination] 
        //// 434f 4c4f 4d42 4941 2032 202d 204d 4553 5349 2030 (Word)
    }
}