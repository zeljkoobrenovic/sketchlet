package net.sf.sketchlet.designer.editor;

import net.sf.sketchlet.context.SketchletContext;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 7-11-12
 * Time: 16:04
 * To change this template use File | Settings | File Templates.
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
                if (SketchletEditor.editorPanel != null) {
                    File files[] = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            try {
                                SketchletEditor.editorPanel.newRegionFromImage(ImageIO.read(file), 0, 0);
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