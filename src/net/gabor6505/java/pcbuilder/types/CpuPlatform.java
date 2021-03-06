package net.gabor6505.java.pcbuilder.types;

import net.gabor6505.java.pcbuilder.xml.*;

import java.util.ArrayList;
import java.util.List;

public class CpuPlatform implements TypeManager.ReloadListener {

    public final static XmlContract CONTRACT = new XmlContract(XmlContract.Folder.TYPES, "cpu_types.xml");
    public final static String[] NODE_NAMES = new String[]{"brand", "socket"};

    private final static List<CpuPlatform> cpuPlatforms = new ArrayList<>(0);

    static {
        TypeManager.addReloadListener(CpuPlatform.class.getName(), new CpuPlatform(null, null, null), 1);
        load();
    }

    private final Brand brand;
    private final String socket;
    private final String prefix;

    private CpuPlatform(Brand brand, String socket, String prefix) {
        this.brand = brand;
        this.socket = socket;
        this.prefix = prefix;
    }

    public Brand getBrand() {
        return brand;
    }

    public String getBrandName() {
        return brand.getName();
    }

    public String getSocket() {
        return socket;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getFormattedSocket() {
        return prefix + socket;
    }

    public static List<CpuPlatform> getCpuPlatforms() {
        return cpuPlatforms;
    }

    public static CpuPlatform getCpuPlatform(String brandName, String socket) {
        for (CpuPlatform type : cpuPlatforms) {
            if (type.getBrandName().equals(brandName) && type.getSocket().equals(socket)) {
                return type;
            }
        }
        new TypeNotPresentException("Cpu Platform", CONTRACT, brandName, socket).printStackTrace();
        return null;
    }

    public static CpuPlatform getCpuPlatform(Brand brand, String socket) {
        return getCpuPlatform(brand.getName(), socket);
    }

    public static CpuPlatform getCpuPlatform(ComponentProperties properties) {
        return getCpuPlatform(properties.getString(0), properties.getString(1));
    }

    public static CpuPlatform getCpuPlatform(Node cpuNode) {
        return getCpuPlatform(cpuNode.getNodesContent(NODE_NAMES));
    }

    private static void load() {
        NodeList root = XmlParser.parseXml(CONTRACT);

        for (Node type : root.getNodes("type")) {
            String brandName = type.getNodeAttributeContent("brand");
            String prefix = type.getNodeAttributeContent("prefix");
            Brand brand = Brand.getBrand(brandName);

            if (brand != null) {
                for (String socket : type.getNodesContent("socket")) {
                    cpuPlatforms.add(new CpuPlatform(brand, socket, prefix));
                }
            }
        }
    }

    @Override
    public void reload() {
        cpuPlatforms.clear();
        load();
    }
}
