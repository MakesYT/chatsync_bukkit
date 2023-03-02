package top.ncserver.chatsync.V2.Until;

import java.util.HashMap;
import java.util.Map;

public class Img {
    Map<Integer, String> packages = new HashMap<>();
    int packageNum = 0;

    public Img(int packageNum, int index, String data) {
        this.packageNum = packageNum;
        packages.put(index, data);
    }

    public void add(int index, String data) {
        if (!packages.containsKey(index))
            packages.put(index, data);
        else {
            packages.remove(index);
            packages.put(index, data);
        }
    }

    public boolean allReceived() {
        return packages.size() == packageNum;
    }

    public String getState() {
        return packages.size() + "/" + packageNum;
    }

    public String getData() {
        StringBuilder sb = new StringBuilder();
        if (allReceived()) {
            for (int i = 0; i < packageNum; i++) {
                try {
                    sb.append(packages.get(i));
                } catch (Exception e) {
                    return "";
                }
            }
            return sb.toString();
        }
        return "";
    }

}
