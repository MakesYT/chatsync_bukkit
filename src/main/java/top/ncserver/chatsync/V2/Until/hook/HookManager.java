package top.ncserver.chatsync.V2.Until.hook;

import java.util.LinkedList;

public class HookManager {
    public static LinkedList<Hook> hooks = new LinkedList<>();

    public static void initHooks() {
        QSHook qsHook = new QSHook();
    }

    public static boolean check(String msg) {
        if (hooks.isEmpty()) return false;
        for (Hook hook : hooks) {
            if (msg.equals(hook.getLastMsg())) {
                return true;
            }
        }
        return false;
    }
}
