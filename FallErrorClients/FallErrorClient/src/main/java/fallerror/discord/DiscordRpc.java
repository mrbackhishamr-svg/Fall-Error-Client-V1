package fallerror.discord;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.net.UnixDomainSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tiny dependency-free Discord Rich Presence client (talks to the Discord app over its local IPC pipe).
 * Setup: put your Discord Application ID in CLIENT_ID and upload the logo as an art asset named "logo".
 */
public final class DiscordRpc {
    public static final String CLIENT_ID = "YOUR_APPLICATION_ID";
    private static final String LOGO_ASSET = "logo";
    private static final String LOGO_TEXT = "Fall Error Client | Made by NoMoreLimits";

    private static volatile String details = "In the menus";
    private static volatile String state = "Made by NoMoreLimits";
    private static volatile boolean running;
    private static Thread thread;

    private DiscordRpc() {}

    public static void update(String d, String s) {
        if (d != null && d.length() >= 2) details = d.length() > 120 ? d.substring(0, 120) : d;
        if (s != null && s.length() >= 2) state = s.length() > 120 ? s.substring(0, 120) : s;
    }

    public static synchronized void start() {
        if (running) return;
        if (CLIENT_ID.startsWith("YOUR_")) {
            System.out.println("[FallError] Discord RPC disabled: set CLIENT_ID in DiscordRpc.java");
            return;
        }
        running = true;
        thread = new Thread(DiscordRpc::loop, "FallError-DiscordRPC");
        thread.setDaemon(true);
        thread.start();
    }

    public static synchronized void stop() {
        running = false;
        if (thread != null) thread.interrupt();
        thread = null;
    }

    // ---------------------------------------------------------------- IO layer
    private interface Io {
        void write(byte[] b) throws IOException;
        void readFully(byte[] b) throws IOException;
        void close();
    }

    private static Io connect() throws IOException {
        boolean win = System.getProperty("os.name").toLowerCase().contains("win");
        for (int i = 0; i < 10; i++) {
            if (win) {
                try {
                    RandomAccessFile f = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
                    return new Io() {
                        public void write(byte[] b) throws IOException { f.write(b); }
                        public void readFully(byte[] b) throws IOException { f.readFully(b); }
                        public void close() { try { f.close(); } catch (IOException ignored) {} }
                    };
                } catch (IOException ignored) {}
            } else {
                for (String dir : unixDirs()) {
                    Path p = Path.of(dir, "discord-ipc-" + i);
                    if (!Files.exists(p)) continue;
                    try {
                        SocketChannel ch = SocketChannel.open(UnixDomainSocketAddress.of(p));
                        return new Io() {
                            public void write(byte[] b) throws IOException {
                                ByteBuffer bb = ByteBuffer.wrap(b);
                                while (bb.hasRemaining()) ch.write(bb);
                            }
                            public void readFully(byte[] b) throws IOException {
                                ByteBuffer bb = ByteBuffer.wrap(b);
                                while (bb.hasRemaining()) if (ch.read(bb) < 0) throw new IOException("closed");
                            }
                            public void close() { try { ch.close(); } catch (IOException ignored) {} }
                        };
                    } catch (IOException ignored) {}
                }
            }
        }
        throw new IOException("Discord is not running");
    }

    private static List<String> unixDirs() {
        List<String> dirs = new ArrayList<>();
        for (String env : new String[]{"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"}) {
            String v = System.getenv(env);
            if (v != null && !v.isEmpty()) {
                dirs.add(v);
                dirs.add(v + "/app/com.discordapp.Discord");        // flatpak
                dirs.add(v + "/snap.discord");                      // snap
            }
        }
        dirs.add("/tmp");
        return dirs;
    }

    private static void send(Io io, int op, String json) throws IOException {
        byte[] d = json.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bb = ByteBuffer.allocate(8 + d.length).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(op).putInt(d.length).put(d);
        io.write(bb.array());
    }

    private static String recv(Io io) throws IOException {
        byte[] h = new byte[8];
        io.readFully(h);
        int len = ByteBuffer.wrap(h).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
        byte[] body = new byte[Math.max(0, Math.min(len, 1 << 20))];
        io.readFully(body);
        return new String(body, StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------- main loop
    private static void loop() {
        long startSec = System.currentTimeMillis() / 1000L;
        while (running) {
            Io io = null;
            try {
                io = connect();
                JsonObject hs = new JsonObject();
                hs.addProperty("v", 1);
                hs.addProperty("client_id", CLIENT_ID);
                send(io, 0, hs.toString());
                recv(io);

                String lastD = null, lastS = null;
                long last = 0;
                while (running) {
                    String d = details, s = state;
                    long now = System.currentTimeMillis();
                    if (!d.equals(lastD) || !s.equals(lastS) || now - last > 15_000) {
                        send(io, 1, activity(d, s, startSec));
                        recv(io);
                        lastD = d; lastS = s; last = now;
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                // Discord closed or not running yet: retry shortly
                try { Thread.sleep(10_000); } catch (InterruptedException ie) { break; }
            } finally {
                if (io != null) io.close();
            }
        }
    }

    private static String activity(String d, String s, long startSec) {
        JsonObject act = new JsonObject();
        act.addProperty("details", d);
        act.addProperty("state", s);
        JsonObject ts = new JsonObject();
        ts.addProperty("start", startSec);
        act.add("timestamps", ts);
        JsonObject assets = new JsonObject();
        assets.addProperty("large_image", LOGO_ASSET);
        assets.addProperty("large_text", LOGO_TEXT);
        act.add("assets", assets);

        JsonObject args = new JsonObject();
        args.addProperty("pid", ProcessHandle.current().pid());
        args.add("activity", act);
        JsonObject root = new JsonObject();
        root.addProperty("cmd", "SET_ACTIVITY");
        root.add("args", args);
        root.addProperty("nonce", UUID.randomUUID().toString());
        return root.toString();
    }
}
