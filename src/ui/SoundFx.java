package ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/** Tiny synthesized sound effects (no audio files needed). Fails silently if no sound device. */
public final class SoundFx {
    private static final int[] NODE_HZ = {262, 330, 392, 523};
    private static volatile boolean enabled = true;

    private SoundFx() { }

    public static void setEnabled(boolean on) { enabled = on; }

    public static void node(int id) { play(new int[]{NODE_HZ[id]}, new int[]{150}); }
    public static void success() { play(new int[]{523, 659}, new int[]{90, 130}); }
    public static void breach() { play(new int[]{392, 523, 659, 784}, new int[]{110, 110, 110, 260}); }
    public static void fail() { play(new int[]{150, 110}, new int[]{220, 450}); }
    public static void win() { play(new int[]{523, 659, 784, 1047, 784, 1047}, new int[]{120, 120, 120, 200, 120, 400}); }

    private static void play(final int[] hz, final int[] ms) {
        if (!enabled) return;
        Thread t = new Thread(() -> {
            try {
                AudioFormat fmt = new AudioFormat(22050f, 8, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
                line.open(fmt, 4096);
                line.start();
                for (int n = 0; n < hz.length; n++) {
                    int len = (int) (22050L * ms[n] / 1000);
                    byte[] buf = new byte[len];
                    for (int i = 0; i < len; i++) {
                        double env = Math.min(1.0, Math.min(i / 200.0, (len - i) / 400.0));
                        buf[i] = (byte) (Math.sin(2 * Math.PI * i * hz[n] / 22050.0) * 55 * env);
                    }
                    line.write(buf, 0, len);
                }
                line.drain();
                line.close();
            } catch (Exception e) {
                // no audio device: ignore
            }
        }, "sound-fx");
        t.setDaemon(true);
        t.start();
    }
}
