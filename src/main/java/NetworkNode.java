import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class NetworkNode {
    public static void main(String[] args) throws IOException {
        // parameter storage
        String gateway_ip = null;
        int gateway_port = 0;
        int port = 0;
        String identifier = null;
        ResourceStorage resourceStorage = new ResourceStorage();
        ArrayList<String> nodesList = new ArrayList<>();

        // Parameter scan loop
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-ident":
                    identifier = args[++i];
                    break;
                case "-gateway":
                    String[] gatewayArray = args[++i].split(":");
                    gateway_ip = gatewayArray[0];
                    gateway_port = Integer.parseInt(gatewayArray[1]);
                    break;
                case "-tcpport":
                    port = Integer.parseInt(args[++i]);
                    break;
                default:
                    String[] resourcesArr = args[i].split(":");
                    resourceStorage.setResourceCount(resourcesArr[0], Integer.parseInt(resourcesArr[1]));
                    break;
            }
        }

        resourceStorage.printState();

        if (gateway_ip != null && gateway_port != 0) {
            nodesList = broadcastNodeIpViaGateway(gateway_ip, gateway_port, port);
        }

        log("Starting nodes IP list");
        log(nodesList.toString());
        ServerSocket serverSocket = new ServerSocket(port);

        String request = "";
        while (!request.equals("TERMINATE") && !request.equals("TERMINATE_NODE")) {
            try {
                log("Accepting messages");
                Socket clientSocket = serverSocket.accept();
                InputStream is = clientSocket.getInputStream();
                OutputStream os = clientSocket.getOutputStream();

                InputStreamReader isr = new InputStreamReader(is);
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedReader br = new BufferedReader(isr);
                BufferedWriter bw = new BufferedWriter(osw);

                request = br.readLine();
                log("New message " + request);
                String[] commands = request.split(" ");
                if (commands[0].equals("NEW_NODE")) {
                    String newNodeIp = clientSocket.getInetAddress().toString().split("/")[1] + ":" + commands[1];
                    log("Received NEW_NODE ip: " + newNodeIp);
                    for (String ip : nodesList) {
                        bw.write(ip);
                        bw.newLine();
                    }

                    broadcastUpdateNodeList(newNodeIp, nodesList);
                    nodesList.add(newNodeIp);
                    log("Current nodes IPs: " + nodesList.toString());
                } else if (commands[0].equals("UPDATE_NODE_LIST")) {
                    nodesList.add(commands[1]);
                    bw.newLine();
                    log("Node list updated: " + nodesList.toString());
                } else {
                    log("Client sent: " + request);
                    String clientId = commands[0];
                    String[] nameCountResourcePairs = Arrays.copyOfRange(commands, 1, commands.length);
                    if (nameCountResourcePairs.length > 0) {
                        boolean resourcesReserved = resourceStorage.reserveResources(clientId, nameCountResourcePairs);

                        if (resourcesReserved) {
                            log("Resources reserved");
                            bw.write("Resources reserved");
                        } else {
                            log("Not enough resources");
                            bw.write("Not enough resources");
                        }

                        resourceStorage.printState();
                    }
                    bw.newLine();
                }

                bw.flush();
                clientSocket.close();
                log("Client socket closed");
            } catch (IOException e) {
                System.err.println("Error " + e.toString());
            }
        }

        if (request.equals("TERMINATE")) {
            broadcastTerminate(nodesList);
        }

        serverSocket.close();
    }

    public static void log(String message) {
        System.out.println("[S]: " + message);
    }

    public static void broadcastTerminate(ArrayList<String> currentNodesIps) {
        for(String currentNodeIp: currentNodesIps) {
            try {
                String[] currentNodeIpParts = currentNodeIp.split(":");
                System.out.println("Broadcasting terminate for current node " + currentNodeIp);
                Socket netSocket = new Socket(currentNodeIpParts[0], Integer.parseInt(currentNodeIpParts[1]));
                PrintWriter out = new PrintWriter(netSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(netSocket.getInputStream()));
                System.out.println("Connected");
                String command = "TERMINATE_NODE";

                System.out.println("Sending: " + command);
                out.println(command);
                out.println();
                // Read and print out the response
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println("Response: " + response);
                }

                // Terminate - close all the streams and the socket
                out.close();
                in.close();
                netSocket.close();
                System.out.println("Terminate broadcasted");
            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + currentNodeIp + ".");
            } catch (IOException e) {
                System.err.println("No connection with " + currentNodeIp + ".");
            }
        }
    }

    public static void broadcastUpdateNodeList(String newNodeIp, ArrayList<String> currentNodesIps) {
        for(String currentNodeIp: currentNodesIps) {
            try {
                String[] currentNodeIpParts = currentNodeIp.split(":");
                System.out.println("Broadcasting update node: " + newNodeIp + " for current node " + currentNodeIp);
                Socket netSocket = new Socket(currentNodeIpParts[0], Integer.parseInt(currentNodeIpParts[1]));
                PrintWriter out = new PrintWriter(netSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(netSocket.getInputStream()));
                System.out.println("Connected");
                String command = "UPDATE_NODE_LIST " + newNodeIp;

                System.out.println("Sending: " + command);
                out.println(command);
                out.println();
                // Read and print out the response
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println("Response: " + response);
                }

                // Terminate - close all the streams and the socket
                out.close();
                in.close();
                netSocket.close();
                System.out.println("IP broadcasted");
            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + currentNodeIp + ".");
            } catch (IOException e) {
                System.err.println("No connection with " + currentNodeIp + ".");
            }
        }
    }

    public static ArrayList<String> broadcastNodeIpViaGateway(String gatewayIp, int gatewayPort, int ownPort) {
        ArrayList<String> nodesIpList = new ArrayList<String>();

        nodesIpList.add(gatewayIp + ":" + gatewayPort);

        // communication socket and streams
        Socket netSocket;
        PrintWriter out;
        BufferedReader in;
        try {
            System.out.println("Connecting with gateway: " + gatewayIp + " at port " + gatewayPort);
            netSocket = new Socket(gatewayIp, gatewayPort);
            out = new PrintWriter(netSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(netSocket.getInputStream()));
            System.out.println("Connected");
            String command = "NEW_NODE " + ownPort;

            System.out.println("Broadcasting ip: " + command);
            out.println(command);
            // Read and print out the response
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("Response: " + response);
                nodesIpList.add(response);
            }

            // Terminate - close all the streams and the socket
            out.close();
            in.close();
            netSocket.close();
            System.out.println("IP broadcasted");
        } catch (UnknownHostException e) {
            System.err.println("Unknown gateway host: " + gatewayIp + ".");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("No connection with gateway " + gatewayIp + ".");
            System.exit(1);
        }

        return nodesIpList;
    }
}


