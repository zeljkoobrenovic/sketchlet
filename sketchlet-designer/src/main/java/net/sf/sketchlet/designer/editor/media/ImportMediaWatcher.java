package net.sf.sketchlet.designer.editor.media;

import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @author zeljko
 */
public class ImportMediaWatcher implements Runnable {
    private static final Logger log = Logger.getLogger(ImportMediaWatcher.class);

    public ImportMediaWatcher() {
        Executors.newCachedThreadPool().execute(this);
    }

    private boolean stopped = false;

    public void run() {
        SketchletContext sketchlet = SketchletContext.getInstance();
        File dir = new File(sketchlet.getUserDirectory(), ".import-media");
        dir.mkdirs();
        while (!stopped) {
            try {
                if (SketchletEditor.getInstance() != null) {
                    File files[] = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            try {
                                SketchletEditor.getInstance().newRegionFromImage(ImageIO.read(file), 0, 0);
                                file.delete();
                            } catch (IOException e) {
                                log.error(e);
                            }
                        }
                    }
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }

    public void stop() {
        this.stopped = true;
    }
}
