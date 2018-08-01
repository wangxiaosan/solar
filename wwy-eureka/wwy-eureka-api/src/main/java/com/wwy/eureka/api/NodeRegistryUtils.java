package com.wwy.eureka.api;

import com.wwy.eureka.api.cluster.Node;
import com.wwy.eureka.api.cluster.NodeConfig;
import com.wwy.eureka.api.cluster.NodeType;

import java.net.Socket;

/**
 * @author wangxiaosan
 * @date 2018/03/07
 */
public class NodeRegistryUtils {
    public static String getRootPath(String clusterName) {
        return "/JPT/" + clusterName + "/NODES";
    }

    private static final String COLON = ":";

    public static String getNodeTypePath(String clusterName, NodeType nodeType) {
        return NodeRegistryUtils.getRootPath(clusterName) + "/" + nodeType;
    }

    public static Node parse(String fullPath) {
        Node node = new Node();
        NodeConfig config = new NodeConfig();
        node.setConfig(config);
        String[] nodeDir = fullPath.split("/");
        NodeType nodeType = NodeType.valueOf(nodeDir[4]);
        config.setNodeType(nodeType);
        String url = nodeDir[5];

        url = url.substring(nodeType.name().length() + 3);
        String address = url.split("\\?")[0];
        String ip = address.split(COLON)[0];

        config.setIp(ip);
        if (address.contains(COLON)) {
            String port = address.split(COLON)[1];
            if (port != null && !"".equals(port.trim())) {
                config.setListenPort(Integer.valueOf(port));
            }
        }
        String params = url.split("\\?")[1];

        String[] paramArr = params.split("&");
        for (String paramEntry : paramArr) {
            String key = paramEntry.split("=")[0];
            String value = paramEntry.split("=")[1];
            if ("clusterName".equals(key)) {
                config.setClusterName(value);
            } else if ("group".equals(key)) {
                config.setNodeGroup(value);
            } else if ("threads".equals(key)) {
                config.setWorkThreads(Integer.valueOf(value));
            } else if ("identity".equals(key)) {
                config.setIdentity(value);
//            } else if ("createTime".equals(key)) {
//                node.setCreateTime(Long.valueOf(value));
            } else if ("isAvailable".equals(key)) {
                config.setAvailable(Boolean.valueOf(value));
            } else if ("serverPort".equals(key)) {
                config.setServerPort(Integer.valueOf(value));
            } else if ("name".equals(key)) {
                config.setName(value);
            }
        }
        return node;
    }

    public static String getFullPath(Node node) {
        StringBuilder path = new StringBuilder();

        NodeConfig config = node.getConfig();
        path.append(getRootPath(config.getClusterName()))
                .append("/")
                .append(config.getNodeType())
                .append("/")
                .append(config.getNodeType())
                .append(":\\\\")
                .append(config.getIp());

        if (config.getListenPort() != 0) {
            path.append(":").append(config.getListenPort());
        }

        path.append("?")
                .append("group=")
                .append(config.getNodeGroup())
                .append("&clusterName=")
                .append(config.getClusterName());
        if (config.getWorkThreads() != 0) {
            path.append("&threads=")
                    .append(config.getWorkThreads());
        }

        path.append("&identity=")
                .append(config.getIdentity())
//                .append("&createTime=")
//                .append(node.getCreateTime())
                .append("&isAvailable=")
                .append(config.isAvailable())
                .append("&serverPort=")
                .append(config.getServerPort())
                .append("&name=")
                .append(config.getName());

        return path.toString();
    }

    public static String getRealRegistryAddress(String registryAddress) {
        return registryAddress;
    }

    public static boolean isExpired(NodeConfig nodeConfig) {
        Socket socket = null;
        try {
            socket = new Socket(nodeConfig.getIp(), nodeConfig.getListenPort());
        } catch (Throwable e) {
            try {
                Thread.sleep(100);
            } catch (Throwable e2) {
            }
            Socket socket2 = null;
            try {
                socket2 = new Socket(nodeConfig.getIp(), nodeConfig.getListenPort());
            } catch (Throwable e2) {
                return true;
            } finally {
                if (socket2 != null) {
                    try {
                        socket2.close();
                    } catch (Throwable e2) {
                    }
                }
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Throwable e) {
                }
            }
        }
        return false;
    }
}
